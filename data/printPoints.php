<?php
include("geohash.php");

$file = fopen("weball.csv","r");
$out = fopen("weballhash.csv", "w");
$count = 0;


$fileFruit = fopen("tree_types.csv","r");
$fruitMap = array();
$count = 0;
while (!feof($fileFruit)) {
	$arr = fgetcsv($fileFruit);
	if ($count != 0) {
		$fruitMap[$arr[1]] = $arr[0];
	}
	$count++;
}
print_r($fruitMap);
fclose($fileFruit);

while(! feof($file))
  {
  	$arr = fgetcsv($file);

	$hash = Geohash::encode($arr[2], $arr[3]);
	$fruitIndex = $fruitMap[$arr[1]];
	fwrite($out, "{$arr[0]},{$fruitIndex},{$arr[2]},{$arr[3]},\"{$arr[4]}\",{$arr[5]},{$arr[6]},{$arr[7]},\"$hash\"\n");
  }

fclose($file);
fclose($out);
?>
