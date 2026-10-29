#!/usr/bin/env python3

r"""Merge three versions of a file of coverage goals.

Usage:
  scripts/merge-coverage-goals.py [--jdks VERSIONS] [--labels BASE_LABEL OURS_LABEL THEIRS_LABEL]
    [--marker-size SIZE] [--stdout] BASE OURS THEIRS

BASE, OURS, and THEIRS are three versions of a file of coverage goals, such as
src/systemTest/resources/test-covgoals/JDKTest.covgoals:  the common ancestor,
the current version, and the other version.  The format of coverage goals is
documented at CoverageChecker.methods().  This script writes the merged goals to
OURS, or to standard output if --stdout is given.  It exits with status 0 if the
merge is clean and status 1 if there are conflicts.

The merge is semantic.  For each method:
 * If ours and theirs have the same lines for the method, or one of them has
   the same lines as base, then the result is the other one's lines, verbatim.
 * Otherwise, if CoverageChecker rejects ours or theirs, there is a conflict.
 * Otherwise, the method's action ("include", "exclude", or "ignore") is merged
   for each JDK version.  If ours and theirs specify the same action, or one of
   them specifies the same action as base, then the merged action is the other
   one's action; otherwise, there is a conflict.  On a JDK version for which
   base has two goals of the same scope, base specifies no action, so there is
   a conflict unless ours and theirs specify the same action.  If there are no
   conflicts, then the result is the first of these candidates that specifies
   the merged actions, that CoverageChecker accepts, and that contains every
   line of the line-by-line merge that has a comment:  the line-by-line merge of
   the method's lines (see below), ours, theirs, and as few lines as possible
   that specify the merged actions.
 * If there is a conflict or no candidate, the result is ours, base, and
   theirs, surrounded by conflict markers.
The JDK versions are those in --jdks VERSIONS, a comma-separated list (default:
8,11,17,21,25,26), plus every version that is mentioned in a goal for the
method.  On other JDK versions, the merged actions may differ.

In a line-by-line merge, a line is in the result if it is in both ours and
theirs, or if one of them added it and the other did not remove it.
Comment-only lines are merged line by line.  They are at the beginning of the
output, in the order in which they appear in ours; each comment-only line that
theirs added follows the merged line that precedes it in theirs.  Duplicate
comment-only lines and blank lines are removed.  The other lines are sorted.

If the semantic merge fails (for example, because a version contains conflict
markers), this script performs a textual merge using "git merge-file".

--labels gives the labels to use in conflict markers; the default is the file
names.  --marker-size gives the length of conflict markers; default: 7.

To use this script as a git merge driver, run these commands in a clone:

  git config merge.covgoals.name "coverage goals merge driver"
  git config merge.covgoals.driver \
    'scripts/merge-coverage-goals.py --labels %S %X %Y --marker-size %L %O %A %B'

and add this line to the .gitattributes file (or to .git/info/attributes):

  *.covgoals merge=covgoals

In Randoop, these steps are already done:  the build runs the "git config"
commands (in the installCovgoalsMergeDriver task, on which compileJava
depends), and the .gitattributes file contains the line.

Git runs the driver in the top-level directory of the working tree, so the
relative file name of this script works only when this script is checked out;
an absolute file name works on every branch.  If the driver is not configured,
git merges *.covgoals files textually.
"""

import argparse
import subprocess
import sys
from pathlib import Path
from typing import TypeVar

from coverage_goals import (
    DEFAULT_JDKS,
    GoalLine,
    action_for,
    is_valid,
    minimal_goals,
    read_goals,
    rejection_reason,
)

T = TypeVar("T")


def merge_value(base: T, ours: T, theirs: T) -> T | None:
    """Return the three-way merge of a value.

    Args:
        base: the value in the common ancestor
        ours: the value in the current version
        theirs: the value in the other version

    Returns:
        the merged value, or None if ours and theirs make different changes
    """
    if ours in (theirs, base):
        return theirs
    if theirs == base:
        return ours
    return None


def merge_lines(base: set[str], ours: set[str], theirs: set[str]) -> set[str]:
    """Return the three-way merge of sets of lines.

    A line is in the result if it is in both ours and theirs, or if one of them
    added it and the other did not remove it.

    Args:
        base: the lines in the common ancestor
        ours: the lines in the current version
        theirs: the lines in the other version

    Returns:
        the merged lines
    """
    return {
        line
        for line in base | ours | theirs
        if merge_value(line in base, line in ours, line in theirs)
    }


def merge_ordered_lines(base: list[str], ours: list[str], theirs: list[str]) -> list[str]:
    """Return the three-way merge of sequences of lines, without duplicates.

    A line is in the result under the same conditions as for merge_lines.  The
    result is in the order of ours; each line that theirs added follows the line
    of the result that precedes it in theirs, or is first if there is none.

    Args:
        base: the lines in the common ancestor
        ours: the lines in the current version
        theirs: the lines in the other version

    Returns:
        the merged lines
    """
    kept = merge_lines(set(base), set(ours), set(theirs))
    result = [line for line in dict.fromkeys(ours) if line in kept]
    # The position in result after the last line of theirs that is in result.
    position = 0
    for line in theirs:
        if line in result:
            position = result.index(line) + 1
        elif line in kept:
            result.insert(position, line)
            position += 1
    return result


def merge_method(
    method: str,
    versions: tuple[list[GoalLine], list[GoalLine], list[GoalLine]],
    jdks: list[int],
) -> tuple[list[str] | None, list[str]]:
    """Merge the lines for a method.

    Args:
        method: a method
        versions: the method's lines in base, ours, and theirs
        jdks: the JDK versions on which to merge the method's actions, in addition to
            every version that is mentioned in a goal for the method.  On each of these
            versions, the merged lines specify the merged action, and CoverageChecker
            accepts them.

    Returns:
        the merged lines, or None if there is a conflict; and the reasons for the conflict
    """
    texts = [sorted(line.text for line in lines) for lines in versions]
    merged_texts = merge_value(*texts)
    if merged_texts is not None:
        return merged_texts, []

    base_goals, ours_goals, theirs_goals = ([line.goal for line in lines] for lines in versions)
    mentioned = {goal.jdk for goal in base_goals + ours_goals + theirs_goals if goal.jdk}
    all_jdks = sorted(set(jdks) | mentioned)
    problems = [
        f"{reason} in {name}"
        for name, goals in (("ours", ours_goals), ("theirs", theirs_goals))
        for jdk in all_jdks
        if (reason := rejection_reason(goals, jdk)) is not None
    ]
    if problems:
        return None, problems

    target = {}
    for jdk in all_jdks:
        ours_action = action_for(ours_goals, jdk)
        theirs_action = action_for(theirs_goals, jdk)
        sides = f"ours {ours_action}, theirs {theirs_action}"
        try:
            base_action = action_for(base_goals, jdk)
            problem = f"JDK {jdk}: base {base_action}, {sides}"
        except ValueError as e:
            # Base's action is unknown, so merge_value succeeds only if ours and theirs agree.
            base_action = None
            problem = f"{e} in base; {sides}"
        action = merge_value(base_action, ours_action, theirs_action)
        if action is None:
            problems.append(problem)
        else:
            target[jdk] = action
    if problems:
        return None, problems

    line_for_text = {line.text: line for lines in versions for line in lines}
    line_merge = [line_for_text[text] for text in merge_lines(*(set(t) for t in texts))]
    comments = {line.text for line in line_merge if line.has_comment}
    minimal = [GoalLine.parse(f"{method} {goal}") for goal in minimal_goals(target)]

    def is_acceptable(candidate: list[GoalLine]) -> bool:
        goals = [line.goal for line in candidate]
        return comments <= {line.text for line in candidate} and all(
            is_valid(goals, jdk) and action_for(goals, jdk) == action
            for jdk, action in target.items()
        )

    for candidate in (line_merge, versions[1], versions[2], minimal):
        if is_acceptable(candidate):
            return sorted(line.text for line in candidate), []
    return None, ["no merge that preserves the comments is correct"]


def merge(
    paths: list[Path], labels: list[str], marker_size: int, jdks: list[int]
) -> tuple[list[str], list[str]]:
    """Merge three versions of a file of coverage goals.

    Args:
        paths: the base, ours, and theirs files
        labels: the labels of base, ours, and theirs for conflict markers
        marker_size: the length of conflict markers
        jdks: the JDK versions on which to merge each method's actions (see merge_method)

    Returns:
        the merged lines, and a textual description of each conflict
    """
    base, ours, theirs = (read_goals(path) for path in paths)

    comment_lines = merge_ordered_lines(
        base.comment_lines, ours.comment_lines, theirs.comment_lines
    )

    # Each entry is a sort key and the lines to output.
    entries: list[tuple[str, list[str]]] = []

    conflicts = []
    # The union of the methods that have lines in any of the three versions.
    methods = set(base.lines_for_method) | set(ours.lines_for_method) | set(theirs.lines_for_method)
    for method in sorted(methods):
        versions = (
            base.lines_for_method.get(method, []),
            ours.lines_for_method.get(method, []),
            theirs.lines_for_method.get(method, []),
        )
        merged, problems = merge_method(method, versions, jdks)
        if merged is not None:
            entries.extend((line, [line]) for line in merged)
            continue
        conflicts.append(f"Conflict in goals for {method}:")
        conflicts.extend(f"  {problem}" for problem in problems)
        base_lines, ours_lines, theirs_lines = (
            sorted(line.text for line in lines) for lines in versions
        )
        block = [
            f"{'<' * marker_size} {labels[1]}",
            *ours_lines,
            f"{'|' * marker_size} {labels[0]}",
            *base_lines,
            "=" * marker_size,
            *theirs_lines,
            f"{'>' * marker_size} {labels[2]}",
        ]
        # Put the conflict where the method's lines would be.
        entries.append((method + " ", block))

    goal_lines = [line for _, lines in sorted(entries) for line in lines]
    return comment_lines + goal_lines, conflicts


def main() -> None:
    """Merge three versions of a file of coverage goals."""
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument("--jdks", default=DEFAULT_JDKS)
    parser.add_argument("--labels", nargs=3, metavar=("BASE_LABEL", "OURS_LABEL", "THEIRS_LABEL"))
    parser.add_argument("--marker-size", type=int, default=7)
    parser.add_argument("--stdout", action="store_true")
    parser.add_argument("base", type=Path)
    parser.add_argument("ours", type=Path)
    parser.add_argument("theirs", type=Path)
    args = parser.parse_args()

    paths = [args.base, args.ours, args.theirs]
    labels = args.labels or [str(path) for path in paths]
    jdks = [int(jdk) for jdk in args.jdks.split(",")]
    try:
        lines, conflicts = merge(paths, labels, args.marker_size, jdks)
    except Exception as e:  # ruff: ignore[blind-except]
        print(f"{type(e).__name__}: {e}; performing a textual merge", file=sys.stderr)
        command = ["git", "merge-file", "--diff3", f"--marker-size={args.marker_size}"]
        for label in (labels[1], labels[0], labels[2]):
            command += ["-L", label]
        command += ["--stdout"] if args.stdout else []
        command += [str(args.ours), str(args.base), str(args.theirs)]
        sys.exit(1 if subprocess.run(command, check=False).returncode else 0)

    output = "".join(line + "\n" for line in lines)
    if args.stdout:
        sys.stdout.write(output)
    else:
        args.ours.write_text(output)
    for conflict in conflicts:
        print(conflict, file=sys.stderr)
    sys.exit(1 if conflicts else 0)


if __name__ == "__main__":
    main()
