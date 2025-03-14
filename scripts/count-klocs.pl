#!/usr/bin/env perl

=head1 NAME

count-klocs.pl

=head1 SYNOPSIS

count-klocs.pl [options] [optional coverage xml file]

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

This perl script is intended for use by Randoop developers to parse
the Randoop code coverage results in order to count the KLOCS of
the portions of Randoop that have been tested. It will write to
standard output a list of Randoop packages and their KLOCs.
By default, the script will read the file:

  build/reports/jacoco/test/jacocoTestReport.xml

This file is generated as part of the gradlew build task. You may generate it directly
with the gradlew jacocoTestReport task.

For more details on the KLOC process see:
https://gitlab.cs.washington.edu/randoop/coverage-tools/blob/master/KLOCS-README.md

=cut

use strict;
use warnings;

use POSIX qw(strftime);
use Getopt::Long qw(GetOptions);
use Pod::Usage qw(pod2usage);
use File::Find;
use File::Temp 'tempfile';

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

# locate the java_count tool
my $java_count = $ENV{'JAVA_COUNT_TOOL'};
if (! defined($java_count)) {
    print "JAVA_COUNT_TOOL environment variable not set\n";
    exit 1;
}

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
if ($details) {
    printf("Processing file: %s\n", $filename);
    print(strftime("Created: %Y-%m-%d %H:%M:%S", localtime((stat($ifh))[9])), "\n");
}

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

my $element;
my $name;
my $rest;
my $targetname;
my $tfh;
my $tfname;

for my $i (0 .. $#xml_lines) {
    $element = $xml_lines[$i];
    if (substr($element, 0, length("package")) eq "package") {
        @fields = split(/"/, $element);
        ($name, $rest) = split(/\//, $fields[1], 2);
        if (!defined($rest)) {
            $rest = " ";
        }
        # printf ("package: %s,%s\n", $name, $rest);
        # get a temp file for the list of corresponding java files we are going to create
        ($tfh, $tfname) = tempfile();
    } elsif (substr($element, 0, length("/package")) eq "/package") {
        # count the klocs in the files we have collected
        # print "end of package\n";

        my $cmd = "$java_count -f $tfname";
        # print "$cmd\n";
        my $output = `$cmd`;
        chomp $output;
        # remove the "Total:" line
        $output =~ s/.*\n//;
        print "$name $rest $output\n";
        close $tfh;
        unlink $tfname;
    } elsif (substr($element, 0, length("class")) eq "class") {
        @fields = split(/"/, $element);
        # print "$fields[1]\n";
        $targetname = "$fields[1].java";
        # skip inner classes
        if ($targetname !~ /\$/) {
            # find and process the matching source file
            find(\&search_for_file, "src/main/java");
        }
    }
}

@fields = split(/"/, $xml_lines[$#xml_lines-4]);

if ($fields[0] ne "counter type=" || $fields[1] ne "LINE") {
    die "Unrecognized format of file '$filename'";
}

close $ifh;

sub search_for_file
{
    my $file = $File::Find::name;
    #print "$file    $targetname\n";
    print $tfh "$file\n" if ($file =~ /$targetname/);
}
