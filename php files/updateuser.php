<?php
include 'db_connect.php';


$response = [];

$email = $_POST['user_email'] ?? null;
$fname = $_POST['user_fname'] ?? null;
$lname = $_POST['user_lname'] ?? null;
$bio   = $_POST['user_biography'] ?? null;

if (!$email || !$fname || !$lname || $bio === null) {
    $response["success"] = false;
    $response["message"] = "Missing fields.";
    echo json_encode($response);
    exit;
}

$sql = "UPDATE USERS SET user_fname = ?, user_lname = ?, user_biography = ? WHERE user_email = ?";
$stmt = mysqli_prepare($link, $sql);
mysqli_stmt_bind_param($stmt, "ssss", $fname, $lname, $bio, $email);

if (mysqli_stmt_execute($stmt)) {
    $response["success"] = true;
    $response["message"] = "Update successful.";
} else {
    $response["success"] = false;
    $response["message"] = "Update failed: " . mysqli_error($link);
}

mysqli_stmt_close($stmt);
mysqli_close($link);

echo json_encode($response);
?>

