<?php
require_once 'common.php';
$inputStream = file_get_contents ( 'php://input' );

try {
	$canDecode = json_decode ( $inputStream, true );
	if ($canDecode === false) {
		throw new Exception ( '', 30002 );
	}
	
	$transactionType = '';

	if(isset($canDecode['head'])){
		$canDecode['head']['username'] = get_eaclient_username();
		$canDecode['head']['uuid'] = get_eaclient_uuid();
		$canDecode['head']['cookieid'] = get_eaclient_cookieid();
		$inputStream = json_encode($canDecode);
		$transactionType = $canDecode['head']['transactiontype'];
	}

	$ctx = stream_context_create ( array ('http' => array ('method' => 'POST', 'timeout' => $_CFG ['CFG_TIMEOUT'], 'header' => 'Content-type: application/x-www-form-urlencoded; charset=utf-8', 'content' => $inputStream ) ) );
	$response = false;

	// Find a available server form zookeeper.
	if(class_exists("Zookeeper")) {
		$zk = new Zookeeper($_CFG ['CFG_ZKHOSTS']);
		if($zk !== false) {
			$serverRoot = '/easyapi/servers';
			$znodes = $zk->getChildren($serverRoot);

			$serverList = array();
			$keys = array();
			$vals = array();

			foreach ($znodes as $node) {
				$val = $zk->get($serverRoot.'/'.$node);
				array_push($keys, $node);
				array_push($vals, $val);
			}

			array_multisort($vals, $keys);
			$serverList = array_combine($keys, $vals);

			foreach ($serverList as $server => $hits) {
				$tmpServer = str_replace('-', ':', $server);
				$tmpServer = 'http://' . $tmpServer . '/apijson.php';
				$response = @file_get_contents ( $tmpServer, 0, $ctx );
				if($response !== false) {
					$zk->set($serverRoot.'/'.$server, ($hits + 1));
					break 1;
				}
			}
		}
	}

	// If get the available server failed from zookeeper. Use the default server address.
	if ($response === false) {
		$response = @file_get_contents ( $_CFG ['CFG_SERVER'], 0, $ctx );
	}

	if ($response === false)
		throw new Exception ( '', 30012 );
	
	ob_clean();
	header("Content-type: application/json");
	echo $response;
} catch ( Exception $ex ) {
	$msg = 'Current host: ' . gethostname();
	exit ( json_encode ( array ('head' => '', 'body' => array ('oelement' => array ('errorcode' => $ex->getCode (), 'errormsg' => $msg ) ) ) ) );
}
