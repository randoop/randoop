"""Coverage goals, as in a file of coverage goals.

The format of coverage goals is documented at CoverageChecker.methods().  This
module is used by update-coverage-goals.py and merge-coverage-goals.py.
"""

import itertools
import re
from collections.abc import Iterable
from dataclasses import dataclass
from pathlib import Path

ACTIONS = ("include", "exclude", "ignore")
# The JDK versions on which the system tests run.
DEFAULT_JDKS = "8,11,17,21,25,26"
# The goal syntax must be consistent with CoverageChecker.methods().
GOAL_PATTERN = re.compile(r"(include|exclude|ignore)(?:(\d+)([+-]?))?")

# The scopes of a goal, from least to most specific.  A goal's scope concerns
# only JDK versions, and it is determined by the goal's form:  no JDK version
# (every version), a JDK version with "+" or "-" (a range of versions), or a JDK
# version alone (a single version).  A more specific goal takes precedence over
# a less specific one.
OVERALL = 0
RANGE = 1
INDIVIDUAL = 2


@dataclass(frozen=True, order=True)
class Goal:
    """A coverage goal for a method, such as "exclude", "ignore17+", or "include25"."""

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


def applicable_goals(goals: Iterable[Goal], jdk: int) -> list[Goal]:
    """Return the given goals for a method that apply to a JDK version.

    Args:
        goals: the goals for a method
        jdk: a JDK version

    Returns:
        the goals that apply to the JDK version, from least to most specific

    Raises:
        ValueError: if two of the goals that apply to the JDK version have the same scope
    """
    goal_for_scope: dict[int, Goal] = {}
    for goal in goals:
        if not goal.applies(jdk):
            continue
        other = goal_for_scope.get(goal.scope())
        if other is not None:
            message = f"JDK {jdk}: goals {other} and {goal} have the same scope"
            raise ValueError(message)
        goal_for_scope[goal.scope()] = goal
    return [goal_for_scope[scope] for scope in sorted(goal_for_scope)]


def action_for(goals: Iterable[Goal], jdk: int) -> str:
    """Return the action that the given goals for a method specify for a JDK version.

    Args:
        goals: the goals for a method
        jdk: a JDK version

    Returns:
        "include", "exclude", or "ignore"

    Raises:
        ValueError: if two of the goals that apply to the JDK version have the same scope
    """  # ruff: ignore[docstring-extraneous-exception]
    applicable = applicable_goals(goals, jdk)
    return applicable[-1].action if applicable else "include"


def is_valid(goals: list[Goal], jdk: int) -> bool:
    """Return true if CoverageChecker accepts the given goals for a method on a JDK version.

    CoverageChecker rejects two goals of the same scope, a goal that specifies the
    action that a less specific goal already specifies, and an unneeded "include".
    If this returns true, then action_for does not raise an exception for the same
    arguments.

    Args:
        goals: the goals for a method
        jdk: a JDK version

    Returns:
        true if CoverageChecker accepts the goals on the JDK version
    """
    try:
        applicable = applicable_goals(goals, jdk)
    except ValueError:
        return False
    actions = ["include"] + [goal.action for goal in applicable]
    return all(prev != action for prev, action in itertools.pairwise(actions))


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
    # The best candidate found so far, as a pair of its sort key and its goals,
    # or None if no candidate has been considered yet.  A smaller key is better.
    best: tuple[tuple[int, int, list[str]], list[Goal]] | None = None
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


def read_goals(path: Path) -> tuple[set[str], dict[str, list[GoalLine]]]:
    """Read a file of coverage goals.

    Args:
        path: a file of coverage goals

    Returns:
        the file's comment-only lines, and a map from each method to its lines
    """
    comment_lines = set()
    lines_for_method: dict[str, list[GoalLine]] = {}
    for line in path.read_text().splitlines():
        if not line.strip():
            continue
        if not line.partition("#")[0].strip():
            comment_lines.add(line)
            continue
        try:
            goal_line = parse_goal_line(line)
        except ValueError as e:
            message = f"{path}: {e}"
            raise ValueError(message) from e
        lines_for_method.setdefault(goal_line.method, []).append(goal_line)
    return comment_lines, lines_for_method
