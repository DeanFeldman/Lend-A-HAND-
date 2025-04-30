<?php
include 'db_connect.php';


$user_email = $_REQUEST['user_email'] ?? null;
error_log("EMAIL RECEIVED: " . $user_email);

if (!$user_email) {
    $response["success"] = false;
    $response["message"] = "No email received.";
    echo json_encode($response);
    exit;
}

$sql = "SELECT user_fname, user_lname, user_biography FROM USERS WHERE user_email = '$user_email'";
$result = mysqli_query($link, $sql);

if (!$result) {
    $response["success"] = false;
    $response["message"] = "Query failed: " . mysqli_error($link);
} elseif ($row = mysqli_fetch_assoc($result)) {
    $response["success"] = true;
    $response["full_name"] = $row["user_fname"] . " " . $row["user_lname"];
    $response["biography"] = $row["user_biography"];
} else {
    $response["success"] = false;
    $response["message"] = "User not found.";
}

mysqli_close($link);
echo json_encode($response);
?>
