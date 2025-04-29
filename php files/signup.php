<?php
include 'db_connect.php';

$user_email = $_REQUEST['user_email'];
$user_password = $_REQUEST['user_password'];

$response = array();

$stmt = mysqli_prepare($link, "SELECT user_ID, user_fname, user_lname, user_dob, user_email, user_password, user_biography FROM USER WHERE user_email = ?");
mysqli_stmt_bind_param($stmt, "s", $user_email);
mysqli_stmt_execute($stmt);
$result = mysqli_stmt_get_result($stmt);

if ($row = mysqli_fetch_assoc($result)) {
    if (password_verify($user_password, $row['user_password'])) {
        $response["success"] = true;
        $response["message"] = "Login successful!";

        // Remove the password from the returned user data
        unset($row['user_password']);

        $response["user"] = $row;
    } else {
        $response["success"] = false;
        $response["message"] = "Incorrect password.";
    }
} else {
    $response["success"] = false;
    $response["message"] = "User not found.";
}

mysqli_stmt_close($stmt);
mysqli_close($link);
echo json_encode($response);
?>
s2698600@csam-server-2:~/public_html$ cat singup.php
cat: singup.php: No such file or directory
s2698600@csam-server-2:~/public_html$ ls
cars2.php  cars.php  db_connect.php  login.php  signup.php  test.php
s2698600@csam-server-2:~/public_html$ cat signup.php
<?php
include 'db_connect.php';

$user_fname = $_REQUEST['user_fname'];
$user_lname = $_REQUEST['user_lname'];
$user_dob = $_REQUEST['user_dob'];
$user_email = $_REQUEST['user_email'];
$user_password = password_hash($_REQUEST['user_password'], PASSWORD_DEFAULT);
$user_biography = $_REQUEST['user_biography'];

$response = array();

// Check if email already exists
$stmt = mysqli_prepare($link, "SELECT user_ID FROM USER WHERE user_email = ?");
mysqli_stmt_bind_param($stmt, "s", $user_email);
mysqli_stmt_execute($stmt);
mysqli_stmt_store_result($stmt);

if (mysqli_stmt_num_rows($stmt) > 0) {
    $response["success"] = false;
    $response["message"] = "Email already registered.";
} else {
    // Insert user
    $insert_stmt = mysqli_prepare($link, "INSERT INTO USER (user_fname, user_lname, user_dob, user_email, user_password, user_biography) VALUES (?, ?, ?, ?, ?, ?)");
    mysqli_stmt_bind_param($insert_stmt, "ssssss", $user_fname, $user_lname, $user_dob, $user_email, $user_password, $user_biography);

    if (mysqli_stmt_execute($insert_stmt)) {
        $response["success"] = true;
        $response["message"] = "User registered successfully!";
    } else {
        $response["success"] = false;
        $response["message"] = "Error: " . mysqli_error($link);
    }
    mysqli_stmt_close($insert_stmt);
}

mysqli_stmt_close($stmt);
mysqli_close($link);
echo json_encode($response);
?>
