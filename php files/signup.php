<?php

include 'db_connect.php';

$user_fname = $_REQUEST['user_fname'] ?? null;
$user_lname = $_REQUEST['user_lname'] ?? null;
$user_dob = $_REQUEST['user_dob'] ?? null;
$user_email = $_REQUEST['user_email'] ?? null;
$user_password = isset($_REQUEST['user_password']) ? password_hash($_REQUEST['user_password'], PASSWORD_DEFAULT) : null;
$user_biography = $_REQUEST['user_biography'] ?? null;

if (!$user_fname || !$user_lname || !$user_dob || !$user_email || !$user_password) {
    echo json_encode([
        "success" => false,
        "message" => "Missing required fields"
    ]);
    exit;
}

$response = array();

$stmt = mysqli_prepare($link, "SELECT user_id FROM USERS WHERE user_email = ?");
mysqli_stmt_bind_param($stmt, "s", $user_email);
mysqli_stmt_execute($stmt);
mysqli_stmt_store_result($stmt);

if (mysqli_stmt_num_rows($stmt) > 0) {
    $response["success"] = false;
    $response["message"] = "Email already registered.";
} else {
    $insert_stmt = mysqli_prepare($link, "INSERT INTO USERS (user_fname, user_lname, user_dob, user_email, user_password, user_biography) VALUES (?, ?, ?, ?, ?, ?)");
    mysqli_stmt_bind_param($insert_stmt, "ssssss", $user_fname, $user_lname, $user_dob, $user_email, $user_password, $user_biography);
    
    if (mysqli_stmt_execute($insert_stmt)) {
        $response["success"] = true;
        $response["message"] = "User registered successfully!";
    } else {
        $response["success"] = false;
        $response["message"] = "Error: " . mysqli_error($link);
    }
    mysqli_stmt_close($insert_stmt);
}

mysqli_stmt_close($stmt);
mysqli_close($link);
echo json_encode($response);
?>
