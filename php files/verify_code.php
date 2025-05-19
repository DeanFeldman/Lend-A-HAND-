<?php
include 'db_connect.php';

$user_email = $_REQUEST['user_email'] ?? null;
$code = $_REQUEST['code'] ?? null;

if (!$user_email || !$code) {
    echo json_encode(["success" => false, "message" => "Missing email or code"]);
    exit;
}


$stmt = $link->prepare("SELECT user_id FROM USERS WHERE user_email = ?");
$stmt->bind_param("s", $user_email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Email not found"]);
    exit;
}

$user_id = $result->fetch_assoc()['user_id'];

$stmt = $link->prepare("SELECT * FROM PASSWORD_RESETS WHERE user_id = ? AND code = ? AND expires_at > NOW()");
$stmt->bind_param("is", $user_id, $code);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Invalid or expired code"]);
    exit;
}

echo json_encode(["success" => true, "message" => "Code verified"]);

$stmt->close();
$link->close();
?>
