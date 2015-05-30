#!/usr/bin/perl

use strict;
use warnings;

my $ps_out_data = `ps -ef | grep java`;
my @ps_out = split(/\n/, $ps_out_data);
my $pid = "";
foreach my $ps_out (@ps_out){
    if($ps_out =~m/type=neurasthenia/){
	if($ps_out =~ m/(\d+)\s+(\d+)/){
	    $pid = $1;
	    last;
	}
    }
}
if($pid){
    `kill -9 $pid`;
    print "Found $pid and kill\n";
}else{
    print "Not found\n";
}

