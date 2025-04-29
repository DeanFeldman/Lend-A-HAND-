<?php
include 'db_connect.php';
// Prepare an array to hold item names
$items = array();

// Query to select all items
$sql = "SELECT * FROM ITEM";
$result = mysqli_query($link, $sql);

if ($result) {
    while ($row = mysqli_fetch_assoc($result)) {
        $items[] = $row['item_name'];
    }
    // Send the items as a JSON array
    echo json_encode($items);
} else {
    echo json_encode(["error" => "No items found"]);
}

// Close the connection
mysqli_close($link);
?>