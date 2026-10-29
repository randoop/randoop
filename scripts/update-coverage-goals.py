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
goals for such a method instead.  In the output file, the comment-only lines
come first, in their original order, followed by the other lines, sorted.

If, on one of these versions, any method in a coverage file has two goals of
the same scope (which CoverageChecker rejects), this script reports every such
method and exits with a nonzero status without changing GOALS_FILE.

For a test whose coverage goals are in RandoopSystemTest.java rather than in a
file, copy the goals to a file, run this script on it, and copy them back.
"""

import argparse
import sys
from pathlib import Path

from coverage_goals import DEFAULT_JDKS, action_for, minimal_goals, read_goals


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

    try:
        contents = read_goals(args.goals_file)
    except ValueError as e:
        sys.exit(str(e))

    methods = sorted(set().union(*(set(c) for c in coverage.values())))
    old_actions_for_method: dict[str, dict[int, str]] = {}
    errors = []
    for method in methods:
        old_goals = [line.goal for line in contents.lines_for_method.get(method, [])]
        try:
            old_actions_for_method[method] = {jdk: action_for(old_goals, jdk) for jdk in jdks}
        except ValueError as e:
            errors.append(f"{args.goals_file}: {method}: {e}")
    if errors:
        sys.exit("\n".join(errors))

    new_lines = {line.text for lines in contents.lines_for_method.values() for line in lines}
    for method in methods:
        old = contents.lines_for_method.get(method, [])
        old_goals = [line.goal for line in old]
        old_actions = old_actions_for_method[method]
        target = dict(old_actions)
        for jdk in jdks:
            if target[jdk] != "ignore" and method in coverage.get(jdk, {}):
                target[jdk] = "include" if coverage[jdk][method] else "exclude"
        if target == old_actions:
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

    output_lines = contents.comment_lines + sorted(new_lines)
    args.goals_file.write_text("".join(line + "\n" for line in output_lines))


if __name__ == "__main__":
    main()
