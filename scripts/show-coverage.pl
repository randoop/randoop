#!/usr/bin/env perl

=head1 NAME

show-coverage.pl

=head1 SYNOPSIS

show-coverage.pl [options] [optional coverage xml file]

 Options:
  -help        brief help message
  -details     include details of each test run
  -man         full documentation

=head1 OPTIONS

=over 4

=item B<-help>

Print a brief help message and exit.

=item B<-details>

Include coverage details for each test.
[default is summary only]

=item B<-man>

Prints the manual page and exits.

=back

=head1 DESCRIPTION

This perl script is intended for use by Randoop developers to display the
Randoop code coverage results.  As part of running the Randoop test suite, the system
uses the Jacoco tool to calculate the amount of the Randoop source code base
that is covered by the generated tests.  The script reads a Randoop tests coverage
file, calculates the percent coverage and displays the result.  By default, the
script will read the file:

  build/reports/jacoco/test/jacocoTestReport.xml

This file is generated as part of the gradlew build task. You may generate it directly
with the gradlew jacocoTestReport task. As part of this testing process, you may
wish to compare previous and current versions of Randoop.  To that end, the script
will accept an alternative file as an argument.

=cut

use strict;
use warnings;

use POSIX qw(strftime);
use Getopt::Long qw(GetOptions);
use Pod::Usage qw(pod2usage);

my $help = 0;
my $details = 0;
my $man = 0;

# Parse options and print usage if there is a syntax error,
# or if usage was explicitly requested.
GetOptions('help|?' => \$help, details => \$details, man => \$man) or pod2usage(2);
pod2usage(1) if $help;
pod2usage(-verbose => 2) if $man;
# Check for too many filenames
pod2usage("$0: Too many files given.\n")  if (@ARGV > 1);

my $test_count = 0;
my $tot_line = 0;
my $tot_exec = 0;
my @xml_lines;
my @fields;
my $filename = 'build/reports/jacoco/test/jacocoTestReport.xml';

print(strftime("\nToday's date: %Y-%m-%d %H:%M:%S", localtime), "\n");

if (@ARGV == 1) {
    $filename = $ARGV[0];
}

open(my $fh, '<', $filename)
  or die "Could not open file '$filename'. $!.\n";
printf("Processing file: %s\n", $filename);
print(strftime("Created: %Y-%m-%d %H:%M:%S", localtime((stat($fh))[9])), "\n");

while (<$fh>) {
    chomp;
    $tot_line++;
    @xml_lines = split /></;
}

if ($tot_line != 1) {
    die "Unrecognized format of file '$filename'";
}

if ($xml_lines[$#xml_lines] ne "/report>") {
    die "Unrecognized format of file '$filename'";
}

if ($details) {
    print "\nTest    Lines     Total    %", "\n";
    print "name    covered   lines    coverage", "\n";

    my $element;
    my $name;
    my @output;
    my $index = 0;
    for my $i (0 .. $#xml_lines) {
        $element = $xml_lines[$i];
        if (substr($element, 0, length("package")) eq "package") {
            @fields = split(/"/, $element);
            $name = $fields[1];
        } elsif (substr($element, 0, length("/package")) eq "/package") {
            @fields = split(/"/, $xml_lines[$i-4]);
            $tot_exec = $fields[5];
            $tot_line = $fields[3] + $tot_exec;
            $output[$index++] = sprintf("%s: %d %d %.2f\n", $name, $tot_exec, $tot_line, $tot_exec/$tot_line);
        }
    }
    foreach (sort(@output)) {
        print $_
    }
}

@fields = split(/"/, $xml_lines[$#xml_lines-4]);

if ($fields[0] ne "counter type=" || $fields[1] ne "LINE") {
    die "Unrecognized format of file '$filename'";
}

$tot_exec = $fields[5];
$tot_line = $fields[3] + $tot_exec;

print "\n";
print "Total lines: ", $tot_line, "\n";
print "Lines executed: ", $tot_exec, "\n";
printf("Coverage: %.2f\n", $tot_exec/$tot_line);
