<?php
    $user = $_GET['username'];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'http://api.beancounter.io/rest/user/'.$user.'/profile?apikey=6da20720-33bf-4bef-bec0-b052980776df');
    curl_setopt($ch, CURLOPT_GET, 1);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $res = curl_exec($ch);

    echo json_encode($res);
?>