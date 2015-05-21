<?php
require_once 'lib/client_common.php';
$_CFG = array();
$confpath = 'config.php';
if(file_exists($confpath)){
	$_CFG = include_once $confpath;
} else {
	exit('Config error');
}
