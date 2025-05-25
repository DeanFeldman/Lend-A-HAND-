<?php
include 'db_connect.php';

$donor_user_id = $_REQUEST['donor_user_id'] ?? null;
$request_id = $_REQUEST['request_id'] ?? null;
$new_quantity = $_REQUEST['new_quantity'] ?? null;
$quantity_donated = $_REQUEST['quantity_donated'] ?? null;

if (!$donor_user_id || !$request_id || !$quantity_donated || $new_quantity === null) {
    echo json_encode(["error" => "Missing required parameters"]);
    exit;
}

$request_id = (int)$request_id;
$new_quantity = (int)$new_quantity;
$quantity_donated = (int)$quantity_donated;
$donor_user_id = (int)$donor_user_id;
$fulfilled = ($new_quantity <= 0) ? 1 : 0;
$donation_date = date("Y-m-d");


$stmt = $link->prepare("UPDATE REQUEST SET quantity_needed = ?, fulfilled = ? WHERE request_id = ?");
if (!$stmt) {
    echo json_encode(["error" => "Prepare failed (REQUEST)", "details" => $link->error]);
    exit;
}
$stmt->bind_param("iii", $new_quantity, $fulfilled, $request_id);
if (!$stmt->execute()) {
    echo json_encode(["error" => "Execution failed(REQUEST)", "details" => $stmt->error]);
    exit;
}


$stmt2 = $link->prepare("SELECT donation_id, quantity_donated FROM DONATION WHERE donor_user_id = ? AND request_id = ?");
if (!$stmt2) {
    echo json_encode(["error" => "Prepare failed (DONATION)", "details" => $link->error]);
    exit;
}
$stmt2->bind_param("ii", $donor_user_id, $request_id);
if (!$stmt2->execute()) {
    echo json_encode(["error" => "Execution failed(DONATION)", "details" => $stmt2->error]);
    exit;
}

$result = $stmt2->get_result();

if ($row = $result->fetch_assoc()) {
    $existing_quantity = (int)$row['quantity_donated'];
    $new_total_quantity = $existing_quantity + $quantity_donated;

    $stmt3 = $link->prepare("UPDATE DONATION SET quantity_donated = ?, donation_date = ? WHERE donation_id = ?");
    if (!$stmt3){
        echo json_encode(["error" => "Update prepare failed (DONATION)", "details" => $link->error]);
        exit;
    }
    $stmt3->bind_param("isi", $new_total_quantity, $donation_date, $row['donation_id']);
    if (!$stmt3->execute()){
        echo json_encode(["error" => "Update failed(DONATION)", "details" => $stmt2->error]);
        exit;
    }

} else {
    $stmt4 = $link->prepare("INSERT INTO DONATION (donor_user_id, request_id, quantity_donated, donation_date) VALUES (?, ?, ?, ?)");
    if (!$stmt4){
        echo json_encode(["error" => "Insert prepare failed (DONATION)", "details" => $link->error]);
        exit;
    }
    $stmt4->bind_param("iiis", $donor_user_id, $request_id, $quantity_donated, $donation_date);
    if (!$stmt4->execute()){
        echo json_encode(["error" => "Insert failed(DONATION)", "details" => $stmt2->error]);;
        exit;
    }
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
