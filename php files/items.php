<?php
include 'db_connect.php';
$items = array();

$sql = "SELECT * FROM ITEM";
$result = mysqli_query($link, $sql);

if ($result) {
    while ($row = mysqli_fetch_assoc($result)) {
        $items[] = $row['item_name'];
    }
        echo json_encode($items);
} else {
    echo json_encode(["error" => "No items found"]);
}

mysqli_close($link);
?>
