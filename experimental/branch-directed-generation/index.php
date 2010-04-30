<?php
  set_include_path (".:/afs/csail.mit.edu/u/j/jhp/phplib");
  include ("TreeMenuXL.php");
  include ("utils.php");
  include ("tasks.php");
?>

<html>
<head>
<title>
Branch Directed Randoop Tasks
</title>
</head>
<script src="http://people.csail.mit.edu/jhp/dhtml/TreeMenu.js"
        language="JavaScript" type="text/javascript"></script>

<body>
<h3 align=center> Branch Directed Randoop Tasks </h3>
<hr>

Open and completed action items and tasks for workshop
The information in brackets for each task is. <ul>
    [<i>Milestone</i>]:<i>resource</i>[<i>Complete]</i>/<i>Duration</i>.
</ul>Durations are in days.  There are
<?php
$today = time();
$meeting = strtotime ("July 18, 2008");
while ($today < $meeting) {
  $info = getdate ($today);
  if (($info['wday'] >=1) && ($info['wday'] <= 5))
    $remaining_days++;
  $today += 24 * 60 * 60;
 }
echo $remaining_days;
?>
 working days remaining until Carlos leaves for the workshop on July 18, 2008.

<?php {
   global $tasks;
   $image_dir = "http://pag.csail.mit.edu/~jhp/images";

   echo "<h4> Workshop and Action Items </h4>\n";

   // Read the tasks file
   $root = read_task_file ("tasks.tko");

   // Dump all of the tasks
   root_tree_menu ($root, "$image_dir/TMimages");

   // Get everything but the completed work
   $current
     = $root->exclude_by_name ("/^Completed Work$|^Milestones$|^Resources$/");
   $current = $current->milestone_match ("/WS/");
   echo "<h4> Remaining Workshop work by resource</h4>\n";
   $res_tree = $current->resource_tree();
   root_tree_menu ($res_tree, "$image_dir/TMimages");

   // Dump out all of the tasks statically
   echo "<h4> All Tasks </h4><ul>\n";
   foreach ($root->tasks as $tid) {
     dump_task ($tasks[$tid]);
   }
   echo "</ul>\n";

}
?>
