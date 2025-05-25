<?php
header('Content-Type: application/json');
include 'db_connect.php';

$user_id = $_REQUEST['user_id'] ?? null;

if (!$user_id) {
    echo json_encode(['success' => false, 'message' => 'Missing user_id']);
    exit;
}

$stmt = $link->prepare("
    SELECT DISTINCT u.user_fname, u.user_lname, u.user_email
    FROM DONATION d
    JOIN REQUEST r ON d.request_id = r.request_id
    JOIN USERS u ON d.donor_user_id = u.user_id
    WHERE r.user_id = ?
");

if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'Prepare failed: ' . $link->error]);
    exit;
}

$stmt->bind_param("i", $user_id);

if (!$stmt->execute()) {
    echo json_encode(['success' => false, 'message' => 'Execute failed: ' . $stmt->error]);
    exit;
}

$result = $stmt->get_result();
$donors = [];

while ($row = $result->fetch_assoc()) {
    $donors[] = [
        'name' => trim($row['user_fname'] . ' ' . $row['user_lname']),
        'email' => $row['user_email']
    ];
}

echo json_encode(['success' => true, 'donors' => $donors]);
?>
