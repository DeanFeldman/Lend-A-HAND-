<?php
include 'db_connect.php';

$user_email = $_REQUEST['user_email'];
$user_password = $_REQUEST['user_password'];

$response = array();

$stmt = mysqli_prepare($link, "SELECT user_ID, user_fname, user_lname, user_dob, user_email, user_password, user_biography FROM USER WHERE user_email = ?");
mysqli_stmt_bind_param($stmt, "s", $user_email);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

if ($row = mysqli_fetch_assoc($result)) {
    if (password_verify($user_password, $row['user_password'])) {
        $response["success"] = true;
        $response["message"] = "Login successful!";

        // Remove the password from the returned user data
        unset($row['user_password']);

        $response["user"] = $row;
    } else {
        $response["success"] = false;
        $response["message"] = "Incorrect password.";
    }
} else {
    $response["success"] = false;
    $response["message"] = "User not found.";
}

mysqli_stmt_close($stmt);
mysqli_close($link);
echo json_encode($response);
?>
