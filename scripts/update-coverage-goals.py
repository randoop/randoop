#!/usr/bin/env python3

r"""Update a file of coverage goals to match measured coverage.

Usage:
  scripts/update-coverage-goals.py [--jdks VERSIONS] GOALS_FILE JDK=COVERAGE_FILE ...

GOALS_FILE is a file of coverage goals, such as
src/systemTest/resources/test-covgoals/JDKTest.covgoals.  The format of
coverage goals is documented at CoverageChecker.methods().

Each JDK=COVERAGE_FILE argument gives the method coverage of a system test when
run on a JDK version.  To create a coverage file, run the system test with the
"randoop.method.coverage.file" system property set to an absolute file name:

  ./gradlew systemTest --rerun --tests randoop.main.RandoopSystemTest.runJDKTest \\
    -PjdkTestVersion=17 -Drandoop.method.coverage.file=$PWD/build/jdk17.txt

The "java" command on PATH must be the same JDK version as -PjdkTestVersion,
because Randoop and the tests that it generates run under that command.

--jdks VERSIONS is a comma-separated list of the JDK versions on which the
system tests run; default: 8,11,17,21,25,26.  For each method whose coverage
differs from its goals on some JDK version, this script rewrites the method's
goals, using as few lines as possible, so that on each of these versions:
 * a method that is ignored remains ignored,
 * on a version that has a coverage file, a method is included if it is
   covered and excluded if it is not, and
 * on a version that has no coverage file, the goal is unchanged.
On other JDK versions, the goals may change.  A method's goals are not
rewritten if any of its lines has a comment; this script prints suggested
goals for such a method instead.  The output file is sorted, so comment-only
lines move to the beginning of the file.

For a test whose coverage goals are in RandoopSystemTest.java rather than in a
file, copy the goals to a file, run this script on it, and copy them back.
"""

import argparse
import itertools
import re
import sys
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path

ACTIONS = ("include", "exclude", "ignore")
# The JDK versions on which the system tests run.
DEFAULT_JDKS = "8,11,17,21,25,26"
# The goal syntax must be consistent with CoverageChecker.methods().
GOAL_PATTERN = re.compile(r"(include|exclude|ignore)(?:(\d+)([+-]?))?")

# The scopes of a goal, from least to most specific.  A more specific goal
# takes precedence over a less specific one.
OVERALL = 0
RANGE = 1
INDIVIDUAL = 2


@dataclass(frozen=True, order=True)
class Goal:
    """A coverage goal for a method, such as "exclude", "ignore17+", or "include11"."""

    action: str
    # The JDK version, or 0 if the goal applies to every version.
    jdk: int = 0
    # "+" (this version and later), "-" (this version and earlier), or "".
    direction: str = ""

    @staticmethod
    def parse(text: str) -> "Goal":
        """Parse a goal.

        Args:
            text: a goal, such as "exclude17+"

        Returns:
            the goal
        """
        m = GOAL_PATTERN.fullmatch(text)
        if m is None:
            message = f"bad coverage goal: {text}"
            raise ValueError(message)
        return Goal(m.group(1), int(m.group(2) or 0), m.group(3) or "")

    def __str__(self) -> str:
        """Format this goal.

        Returns:
            this goal in the format of a coverage goals file, such as "exclude17+"
        """
        return self.action + (str(self.jdk) if self.jdk else "") + self.direction

    def scope(self) -> int:
        """Return the scope of this goal.

        Returns:
            OVERALL, RANGE, or INDIVIDUAL
        """
        if self.jdk == 0:
            return OVERALL
        return RANGE if self.direction else INDIVIDUAL

    def applies(self, jdk: int) -> bool:
        """Return true if this goal applies to the given JDK version.

        Args:
            jdk: a JDK version

        Returns:
            true if this goal applies to the JDK version
        """
        if self.jdk == 0 or jdk == self.jdk:
            return True
        if self.direction == "+":
            return jdk > self.jdk
        if self.direction == "-":
            return jdk < self.jdk
        return False


def action_for(goals: Iterable[Goal], jdk: int) -> str:
    """Return the action that the given goals for a method specify for a JDK version.

    Args:
        goals: the goals for a method
        jdk: a JDK version

    Returns:
        "include", "exclude", or "ignore"
    """
    result = "include"
    result_scope = -1
    for goal in goals:
        if goal.applies(jdk) and goal.scope() > result_scope:
            result = goal.action
            result_scope = goal.scope()
    return result


def minimal_goals(target: dict[int, str]) -> list[Goal]:
    """Return as few goals as possible that specify the given action for each JDK version.

    The goals are acceptable to CoverageChecker:  no two range goals apply to
    the same JDK version, and no goal specifies the action that a less
    specific goal already specifies.

    Args:
        target: a map from JDK version to action

    Returns:
        the goals
    """
    jdks = sorted(target)
    best: tuple | None = None
    for overall in ACTIONS:
        others = [action for action in ACTIONS if action != overall]
        ranges = [None, *itertools.product(jdks, others)]
        for lower, upper in itertools.product(ranges, ranges):
            if lower is not None and upper is not None and lower[0] >= upper[0]:
                continue
            goals = [] if overall == "include" else [Goal(overall)]
            if lower is not None:
                goals.append(Goal(lower[1], lower[0], "-"))
            if upper is not None:
                goals.append(Goal(upper[1], upper[0], "+"))
            for jdk in jdks:
                if action_for(goals, jdk) != target[jdk]:
                    goals.append(Goal(target[jdk], jdk))
            num_ranges = sum(1 for goal in goals if goal.scope() == RANGE)
            # Prefer fewer goals, then fewer range goals.
            key = (len(goals), num_ranges, [str(goal) for goal in goals])
            if best is None or key < best[0]:
                best = (key, goals)
    assert best is not None
    return sorted(best[1], key=str)


@dataclass
class GoalLine:
    """A line of a coverage goals file."""

    text: str
    method: str
    goal: Goal
    has_comment: bool


def parse_goal_line(text: str) -> GoalLine:
    """Parse a line of a coverage goals file.

    Args:
        text: a line of a coverage goals file, which is not blank or comment-only

    Returns:
        the parsed line
    """
    body, _, comment = text.partition("#")
    method, _, goal = body.strip().rpartition(" ")
    return GoalLine(text, method, Goal.parse(goal), bool(comment.strip()))


def read_coverage(path: Path) -> dict[str, bool]:
    """Read a coverage file.

    Args:
        path: a coverage file, each of whose lines is "covered METHOD" or "uncovered METHOD"

    Returns:
        a map from each method to whether it is covered
    """
    result = {}
    for line in path.read_text().splitlines():
        status, _, method = line.partition(" ")
        if status not in ("covered", "uncovered") or not method.strip():
            sys.exit(f"{path}: bad line: {line}")
        result[method] = status == "covered"
    return result


def main() -> None:
    """Update a file of coverage goals."""
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--jdks", default=DEFAULT_JDKS)
    parser.add_argument("goals_file", type=Path)
    parser.add_argument("coverage", nargs="+", metavar="JDK=COVERAGE_FILE")
    args = parser.parse_args()

    jdks = [int(jdk) for jdk in args.jdks.split(",")]
    coverage: dict[int, dict[str, bool]] = {}
    for arg in args.coverage:
        jdk, _, path = arg.partition("=")
        if not jdk.isdigit() or not path:
            sys.exit(f"bad argument, not JDK=COVERAGE_FILE: {arg}")
        if int(jdk) not in jdks:
            sys.exit(f"JDK {jdk} is not in --jdks {args.jdks}")
        coverage[int(jdk)] = read_coverage(Path(path))

    lines = [line for line in args.goals_file.read_text().splitlines() if line.strip()]
    lines_for_method: dict[str, list[GoalLine]] = {}
    for line in lines:
        if not line.partition("#")[0].strip():
            continue
        goal_line = parse_goal_line(line)
        lines_for_method.setdefault(goal_line.method, []).append(goal_line)

    methods = sorted(set().union(*(set(c) for c in coverage.values())))
    new_lines = set(lines)
    for method in methods:
        old = lines_for_method.get(method, [])
        old_goals = [line.goal for line in old]
        target = {}
        for jdk in jdks:
            target[jdk] = action_for(old_goals, jdk)
            if target[jdk] != "ignore" and method in coverage.get(jdk, {}):
                target[jdk] = "include" if coverage[jdk][method] else "exclude"
        if all(action_for(old_goals, jdk) == target[jdk] for jdk in jdks):
            continue
        new_goals = minimal_goals(target)
        new = [f"{method} {goal}" for goal in new_goals]
        if any(line.has_comment for line in old):
            print(f"Not changing commented goals for {method}.  Suggested goals:")
            print("\n".join(f"  {line}" for line in new) if new else "  (none)")
            continue
        new_lines.difference_update(line.text for line in old)
        new_lines.update(new)
        print(f"{method}:")
        print(f"  old: {', '.join(str(goal) for goal in old_goals) or '(none)'}")
        print(f"  new: {', '.join(str(goal) for goal in new_goals) or '(none)'}")

    args.goals_file.write_text("".join(line + "\n" for line in sorted(new_lines)))


if __name__ == "__main__":
    main()
