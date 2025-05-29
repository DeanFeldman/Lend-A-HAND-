<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);
include 'db_connect.php';
$donor_user_id = $_REQUEST['donor_user_id'] ?? null;
$request_id = $_REQUEST['request_id'] ?? null;
$quantity_donated = $_REQUEST['quantity_donated'] ?? null;

if (!$donor_user_id || !$request_id || !$quantity_donated) {
    echo json_encode(["error" => "Missing required parameters"]);
    exit;
}

$donor_user_id = (int)$donor_user_id;
$request_id = (int)$request_id;
$quantity_donated = (int)$quantity_donated;

$stmt = $link->prepare("INSERT INTO DONATION (donor_user_id, request_id, quantity_donated) VALUES (?, ?, ?)");
if (!$stmt) {
    echo json_encode(["error" => "Insert prepare failed", "details" => $link->error]);
    exit;
}
$stmt->bind_param("iii", $donor_user_id, $request_id, $quantity_donated);
if (!$stmt->execute()) {
    echo json_encode(["error" => "Insert failed", "details" => $stmt->error]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Donation recorded. Update REQUEST using update_request_quantity.php"
]);
?>