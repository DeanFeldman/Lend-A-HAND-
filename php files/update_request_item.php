<?php
// update_request_quantity.php
error_reporting(E_ALL);
ini_set('display_errors', 1);
include 'db_connect.php';


$request_id = $_REQUEST['request_id'] ?? null;
$new_quantity = $_REQUEST['new_quantity'] ?? null;

if ($request_id === null || $new_quantity === null) {
    echo json_encode(["error" => "Missing request_id or new_quantity"]);
    exit;
}

$request_id = (int)$request_id;
$new_quantity = (int)$new_quantity;
$fulfilled = ($new_quantity <= 0) ? 1 : 0;

$stmt = $link->prepare("UPDATE REQUEST SET quantity_needed = ?, fulfilled = ? WHERE request_id = ?");
if (!$stmt) {
    echo json_encode(["error" => "Prepare failed", "details" => $link->error]);
    exit;
}

$stmt->bind_param("iii", $new_quantity, $fulfilled, $request_id);
if (!$stmt->execute()) {
    echo json_encode(["error" => "Execution failed", "details" => $stmt->error]);
    exit;
}

if ($stmt->affected_rows === 0) {
    echo json_encode(["error" => "No rows updated. Invalid request_id?", "request_id" => $request_id]);
    exit;
}

echo json_encode([
    "success" => true,
    "message" => "Request updated successfully",
    "request_id" => $request_id,
    "new_quantity" => $new_quantity,
    "fulfilled" => $fulfilled
]);
?>