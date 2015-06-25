<?php
/**
 * Functions for client
 */
function get_eaclient_username(){
	if(empty($_COOKIE['USER_NAME']))
		return '';
	else 
		return $_COOKIE['USER_NAME'];
}

function get_eaclient_uuid(){
	if(empty($_COOKIE['USER_ID']))
		return '';
	else 
		return $_COOKIE['USER_ID'];
}

function get_eaclient_cookieid(){
	if(empty($_COOKIE['faid']))
		return '';
	else 
		return $_COOKIE['faid'];
}
