<?php

$file = fopen("weball.csv","r");
$foodTypes = array();
while(! feof($file)) {
$arr = fgetcsv($file);
  if (!in_array($arr[1], $foodTypes)) {
    $foodTypes[] = $arr[1];
  }
}
asort($foodTypes);
$count = 1;
print "ft_id,ft_name,ft_info_url,ft_image_url";
foreach ($foodTypes as $food) {
	print ("$count,$food,\"http:/en.wikipedia.org/wiki/$food\"\n");
	$count++;
}
fclose($file);

?>
