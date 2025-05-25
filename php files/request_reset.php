<?php
header('Content-Type: application/json');
include 'db_connect.php';

$user_email = $_REQUEST['user_email'] ?? null;

if (!$user_email) {
    echo json_encode(["success" => false, "message" => "Missing email"]);
    exit;
}

$stmt = $link->prepare("SELECT user_id, user_fname FROM USERS WHERE user_email = ?");
$stmt->bind_param("s", $user_email);
$stmt->execute();
$result = $stmt->get_result();

if ($result->num_rows === 0) {
    echo json_encode(["success" => false, "message" => "Email not found"]);
    exit;
}

$row = $result->fetch_assoc();
$user_id = $row['user_id'];
$first_name = $row['user_fname'];

$code = str_pad(random_int(0, 999999), 6, '0', STR_PAD_LEFT);
$expires_at = date("Y-m-d H:i:s", time() + 15 * 60);

$stmt = $link->prepare("REPLACE INTO PASSWORD_RESETS (user_id, code, expires_at) VALUES (?, ?, ?)");
$stmt->bind_param("iss", $user_id, $code, $expires_at);
$stmt->execute();

echo json_encode([
    "success" => true,
    "message" => "Code generated",
    "code" => $code,
    "first_name" => $first_name
]);

$stmt->close();
$link->close();
?>
