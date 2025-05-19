<?php
$username = "s2698600";
$password = "s2698600";
$database = "d2698600";

$link = mysqli_connect("127.0.0.1", $username, $password, $database);

if (!$link) {
    die("Connection failed: " . mysqli_connect_error());
}
?>
