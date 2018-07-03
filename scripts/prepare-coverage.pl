#!/usr/bin/env perl

=head1 NAME

prepare-coverage.pl

=head1 SYNOPSIS

prepare-coverage.pl [options] [optional coverage xml file]

 Options:
  -help        brief help message
  -man         full documentation

=head1 OPTIONS

=over 4

=item B<-help>

Print a brief help message and exit.

=item B<-man>

Prints the manual page and exits.

=back

=head1 DESCRIPTION

This perl script is intended for use by Randoop developers to convert
the Randoop code coverage results into a csv file suitable for input to
"Randoop Coverage.xlsm".  The output is written to the file named
"report-<input file modifcation date>.csv" in the current directory.
By default, the script will read the file:

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
my $man = 0;

# Parse options and print usage if there is a syntax error,
# or if usage was explicitly requested.
GetOptions('help|?' => \$help, man => \$man) or pod2usage(2);
pod2usage(1) if $help;
pod2usage(-verbose => 2) if $man;
# Check for too many filenames
pod2usage("$0: Too many files given.\n")  if (@ARGV > 1);

my $tot_line = 0;
my $tot_exec = 0;
my @xml_lines;
my @fields;
my $filename = 'build/reports/jacoco/test/jacocoTestReport.xml';

if (@ARGV == 1) {
    $filename = $ARGV[0];
}

open(my $ifh, '<', $filename)
  or die "Could not open file '$filename'. $!.\n";
printf("\nProcessing file: %s\n", $filename);
print(strftime("Created: %Y-%m-%d %H:%M:%S", localtime((stat($ifh))[9])), "\n");

my $outfilename = "report-" . strftime("%Y%m%d", localtime((stat($ifh))[9])) . ".csv";
open(my $ofh, '>', $outfilename)
  or die "Could not open file '$outfilename' $!";
printf $ofh ("%s, %s\n", $filename, strftime("%Y-%m-%d %H:%M:%S", localtime((stat($ifh))[9])));

while (<$ifh>) {
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

print $ofh "project,case,covered lines,total lines\n";

my $element;
my $name;
my $rest;
for my $i (0 .. $#xml_lines) {
    $element = $xml_lines[$i];
    if (substr($element, 0, length("package")) eq "package") {
        @fields = split(/"/, $element);
        ($name, $rest) = split(/\//, $fields[1], 2);
        if (!defined($rest)) {
            $rest = " ";
        }
    } elsif (substr($element, 0, length("/package")) eq "/package") {
        @fields = split(/"/, $xml_lines[$i-4]);
        $tot_exec = $fields[5];
        $tot_line = $fields[3] + $tot_exec;
        printf $ofh ("%s,%s,%d,%d\n", $name, $rest, $tot_exec, $tot_line);
    }
}

@fields = split(/"/, $xml_lines[$#xml_lines-4]);

if ($fields[0] ne "counter type=" || $fields[1] ne "LINE") {
    die "Unrecognized format of file '$filename'";
}

close $ifh;
close $ofh;
