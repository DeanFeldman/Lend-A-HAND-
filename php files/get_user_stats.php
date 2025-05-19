<?php
include 'db_connect.php';

$user_id = $_REQUEST['user_id'] ?? null;
if (!$user_id) {
    echo json_encode(["error" => "Missing user_id"]);
    exit;
}

$user_id = (int)$user_id;
$response = ["donated" => [], "received" => []];


$donated_stmt = $link->prepare("
    SELECT i.item_name, SUM(d.quantity_donated) AS total_donated
    FROM DONATION d
    JOIN REQUEST r ON d.request_id = r.request_id
    JOIN ITEM i ON r.item_id = i.item_id
    WHERE d.donor_user_id = ?
    GROUP BY i.item_name
");

if ($donated_stmt) {
    $donated_stmt->bind_param("i", $user_id);
    $donated_stmt->execute();
    $result = $donated_stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $response["donated"][] = $row;
    }
    $donated_stmt->close();
}


$received_stmt = $link->prepare("
    SELECT i.item_name, SUM(d.quantity_donated) AS total_received
    FROM DONATION d
    JOIN REQUEST r ON d.request_id = r.request_id
    JOIN ITEM i ON r.item_id = i.item_id
    WHERE r.user_id = ?
    GROUP BY i.item_name
");


if ($received_stmt) {
    $received_stmt->bind_param("i", $user_id);
    $received_stmt->execute();
    $result = $received_stmt->get_result();
    while ($row = $result->fetch_assoc()) {
        $response["received"][] = [
            "item_name" => $row["item_name"],
            "total_received" => $row["total_received"]
        ];
    }
    $received_stmt->close();
}

echo json_encode($response);
?>
