<?php

include 'db_connect.php';

$item_name = $_REQUEST['item_name'] ?? null;

if (!$item_name) {
    echo json_encode(["error" => "Missing item_name"]);
    exit;
}

$data = array();

$stmt = $link->prepare("
    SELECT 
        r.request_id,
        u.user_id,
        u.user_fname,
        u.user_lname,
        u.user_biography,
        u.user_email,
        r.quantity_needed
    FROM REQUEST r
    JOIN USERS u ON u.user_id = r.user_id
    JOIN ITEM i ON r.item_id = i.item_id
    WHERE i.item_name = ?
    AND r.fulfilled = 0
");

$stmt->bind_param("s", $item_name);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $data[] = [
        "request_id" => $row["request_id"],
        "user_id" => $row["user_id"],
        "user_fname" => $row["user_fname"],
        "user_lname" => $row["user_lname"],
        "user_biography" => $row["user_biography"],
        "user_email"=>$row["user_email"],
        "quantity_needed" => $row["quantity_needed"]
    ];
}

echo json_encode($data);
?>
