<?php
error_reporting(E_ALL);
ini_set('display_errors', 1);

include 'db_connect.php';

$item_name =$_REQUEST['item_name'];

if (!isset($item_name)) {
    echo json_encode(["error" => "Missing item_name"]);
    exit;
}

$response = array();
$data = array();

$stmt = $link->prepare("
    SELECT u.user_fname, u.user_lname, u.user_biography, r.quantity_needed
    FROM USERS u
    JOIN REQUEST r ON u.user_id = r.user_id
    JOIN ITEM i ON r.item_id = i.item_ID
    WHERE i.item_name = ?
");
$stmt->bind_param("s", $item_name);
$stmt->execute();
$result = $stmt->get_result();

while ($row = $result->fetch_assoc()) {
    $data[] = $row;
}

echo json_encode($data);
?>