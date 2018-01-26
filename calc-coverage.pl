#!/usr/bin/env perl
use strict;
use warnings;

use POSIX qw(strftime);

# perl script to display Randoop internal tests coverage file and calculate the percent coverage

my $tot_line;
my @xml_lines;

    print(strftime("Today's date: %Y-%m-%d %H:%M:%S", localtime), "\n");

    my $filename = 'build/reports/jacoco/test/jacocoTestReport.xml';
    open(my $fh, '<', $filename)
      or die "Could not open file '$filename'. $!.\n";
    printf("Processing file: %s\n", $filename);
    print(strftime("Created: %Y-%m-%d %H:%M:%S", localtime((stat($fh))[9])), "\n");

    while (<$fh>) {
        chomp;
#       print $_;
        $tot_line++;
        @xml_lines = split /></;
    }

    if ($tot_line != 1) {
        die "Unrecognized format of file '$filename'";
    }

#   printf("\nnum xml_lines %s\n", $#xml_lines);
    if ($xml_lines[$#xml_lines] ne "/report>") {
        die "Unrecognized format of file '$filename'";
    }

#   print $xml_lines[$#xml_lines-4], "\n";
    my @fields = split(/"/, $xml_lines[$#xml_lines-4]);

    if ($fields[0] ne "counter type=" || $fields[1] ne "LINE") {
        die "Unrecognized format of file '$filename'";
    }

    my $tot_exec = $fields[5];
    $tot_line = $fields[3] + $tot_exec;

    print "Total lines: ", $tot_line, "\n";
    print "Lines executed: ", $tot_exec, "\n";
    printf("Coverage: %.2f\n", $tot_exec/$tot_line);
