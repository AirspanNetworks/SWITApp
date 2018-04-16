<?php

define("UPLOAD_DIR", "/home/www/html/upload");
function extractIPfromFileName($fname){
  $pattern = '/[_-]/';
  $arrayNames = preg_split($pattern, $fname);	
  $folderName = $arrayNames[1];
  if (strpos($folderName, '.') == false)
	$folderName = '';
  echo "folder Name == $folderName\n";	
 return $folderName;
}
function getBoolStr($value){
	return $value ? "true" : "false";
}

function createDirAndGrantPermissions0777($fullDirPath){
	$retVal = mkdir($fullDirPath);
	echo "mkdir on " . $fullDirPath . " status: " . getBoolStr($retVal) . "\n";

	$retVal = chmod($fullDirPath, 0777);
	echo "chmod on " . $fullDirPath . " status: " . getBoolStr($retVal) . "\n";
}
	
//checking file isn't empty
if (!empty($_FILES["file"])) {
    $File = $_FILES["file"];

	//checking for no errors
    if ($File["error"] !== UPLOAD_ERR_OK) {
        echo "An error occurred.\n";
        exit;
    }

    // ensure a safe filename
    $name = preg_replace("/[^A-Z0-9._-]/i", "_", $File["name"]);

    // don't overwrite an existing file
    $i = 0;
    $parts = pathinfo($name);
    while (file_exists(UPLOAD_DIR . "/" . $name)) {
        $i++;
        $name = $parts["filename"] . "-" . $i . "." . $parts["extension"];
    }
	//get date with the format dd.mm.yy
	$folderDateName = date("d.m.y");
	$folderIPName = extractIPfromFileName($name);
	
	echo "folder with ip = $folderIPName\n";
	//cases for folders
	if(file_exists(UPLOAD_DIR . "/" . $folderDateName)){
		echo "folder " . UPLOAD_DIR . "/" . $folderDateName ." exists.\n";
		if(file_exists(UPLOAD_DIR . "/" . $folderDateName . "/" . $folderIPName)){
			$success = move_uploaded_file($File["tmp_name"],UPLOAD_DIR . "/" . $folderDateName."/" . $folderIPName. "/" .$name);
			if(!$success){
				echo "faild move file to  " . UPLOAD_DIR . "/"  .$folderDateName."/" . $folderIPName. "/" .$name . "\n";
			} 
		}
		else{
			//there is no IP folder in date folder.
			echo "there is no IP Folder - creating it\n";
			createDirAndGrantPermissions0777(UPLOAD_DIR . "/" . $folderDateName . "/" . $folderIPName);
			echo "created ip folder ".UPLOAD_DIR . "/" . $folderDateName . "/" . $folderIPName."\n";

			$success = move_uploaded_file($File["tmp_name"],$folderDateName."/" . $folderIPName. "/" .$name);
			if(!$success){
				echo "faild move file to  " . UPLOAD_DIR . "/" .$folderDateName."/" . $folderIPName. "/" .$name ."after creating Ip Folder\n";
			} 
		}
	}
	else{
		//no date folder and no IP folder
		createDirAndGrantPermissions0777(UPLOAD_DIR . "/" . $folderDateName);
		echo "created date folder - " . UPLOAD_DIR . "/" . $folderDateName . "\n";

		createDirAndGrantPermissions0777(UPLOAD_DIR . "/" . $folderDateName . "/" . $folderIPName);
		echo "created ip folder in - " . UPLOAD_DIR . "/" . $folderDateName . "/" . $folderIPName ."\n";


		$success = move_uploaded_file($File["tmp_name"],UPLOAD_DIR . "/" . $folderDateName ."/" . $folderIPName. "/" .$name);
		if(!$success){
			echo "faild move file to  " . UPLOAD_DIR . "/" . $folderDateName ."/" . $folderIPName. "/" .$name . "after creating Ip Folder\n";
		} 
		
	}

	
	   exit;
}
?>
