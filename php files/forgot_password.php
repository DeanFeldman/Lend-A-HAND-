<?php
include 'db_connect.php';

$user_email = $_REQUEST['user_email'] ?? null;
$new_password = $_REQUEST['new_password'] ?? null;

if (!$user_email || !$new_password) {
    echo json_encode(["success" => false, "message" => "Missing email or new password"]);
    exit;
}

$hashed_password = password_hash($new_password, PASSWORD_DEFAULT);

$stmt = $link->prepare("UPDATE USERS SET user_password = ? WHERE user_email = ?");
$stmt->bind_param("ss", $hashed_password, $user_email);

if ($stmt->execute()) {
    if ($stmt->affected_rows > 0) {
        echo json_encode(["success" => true, "message" => "Password updated successfully"]);
    } else {
        echo json_encode(["success" => false, "message" => "Email not found"]);
    }
} else {
    echo json_encode(["success" => false, "message" => "Error updating password"]);
}

$stmt->close();
$link->close();
?>
