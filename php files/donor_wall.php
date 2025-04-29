<?php
include 'db_connect.php';

if (!$link) {
    die(json_encode(["success" => false, "message" => "Connection failed: " . mysqli_connect_error()]));
}

$sql = "SELECT
            u.user_fname AS first_name,
            u.user_lname AS last_name,
            SUM(d.quantity_donated) AS total_donated
        FROM DONATION d
        JOIN USERS u ON d.donor_user_id = u.user_id
        GROUP BY d.donor_user_id
        ORDER BY total_donated DESC";

$result = mysqli_query($link, $sql);


if (!$result) {
    echo json_encode([
        "success" => false,
        "message" => "Query failed: " . mysqli_error($link)
    ]);
    exit;
}

$leaderboard = [];

while ($row = mysqli_fetch_assoc($result)) {
    $leaderboard[] = [
        "first_name" => $row["first_name"],
        "last_name" => $row["last_name"],
        "total_donated" => (int)$row["total_donated"]
    ];
}

echo json_encode([
    "success" => true,
    "leaderboard" => $leaderboard
]);

mysqli_close($link);
?>