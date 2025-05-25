<?php
include 'db_connect.php';

$user_id = isset($_REQUEST['user_id']) ? intval($_REQUEST['user_id']) : 0;
$item_name = isset($_REQUEST['item_name']) ? trim($_REQUEST['item_name']) : '';
$quantity_needed = isset($_REQUEST['quantity_needed']) ? intval($_REQUEST['quantity_needed']) : 0;

$response = array();

if ($user_id <= 0 || empty($item_name) || $quantity_needed <= 0) {
    $response["success"] = false;
    $response["message"] = "Invalid input parameters.";
    echo json_encode($response);
    exit;
}

$stmt = mysqli_prepare($link, "SELECT item_id FROM ITEM WHERE item_name = ?");
mysqli_stmt_bind_param($stmt, "s", $item_name);
mysqli_stmt_execute($stmt);
mysqli_stmt_bind_result($stmt, $item_id);
$found = mysqli_stmt_fetch($stmt);
mysqli_stmt_close($stmt);

if (!$found) {
    $response["success"] = false;
    $response["message"] = "Item not found: " . $item_name;
    echo json_encode($response);
    mysqli_close($link);
    exit;
}

$dstmt = mysqli_prepare($link, "SELECT quantity_needed FROM REQUEST WHERE user_id = ? AND item_id = ?");
mysqli_stmt_bind_param($dstmt, "ii", $user_id, $item_id);
mysqli_stmt_execute($dstmt);
mysqli_stmt_bind_result($dstmt, $existing_quantity);
$exists = mysqli_stmt_fetch($dstmt);
mysqli_stmt_close($dstmt);

if ($exists) {
    $new_quantity = $existing_quantity + $quantity_needed;

    $update_stmt = mysqli_prepare($link, "UPDATE REQUEST SET quantity_needed = ? WHERE user_id = ? AND item_id = ?");
    mysqli_stmt_bind_param($update_stmt, "iii", $new_quantity, $user_id, $item_id);

    if (mysqli_stmt_execute($update_stmt)) {
        $response["success"] = true;
        $response["message"] = "Quantity updated successfully!";
    } else {
        $response["success"] = false;
        $response["message"] = "Error updating quantity: " . mysqli_error($link);
    }
    mysqli_stmt_close($update_stmt);
} else {
    $insert_stmt = mysqli_prepare($link, "INSERT INTO REQUEST (user_id, item_id, quantity_needed) VALUES (?, ?, ?)");
    mysqli_stmt_bind_param($insert_stmt, "iii", $user_id, $item_id, $quantity_needed);

    if (mysqli_stmt_execute($insert_stmt)) {
        $response["success"] = true;
        $response["message"] = "Request submitted successfully!";
    } else {
        $response["success"] = false;
        $response["message"] = "Error inserting request: " . mysqli_error($link);
    }
    mysqli_stmt_close($insert_stmt);
}

echo json_encode($response);
mysqli_close($link);
?>

