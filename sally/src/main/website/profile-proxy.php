<?php
    $user = $_GET['username'];

    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, 'http://api.beancounter.io/rest/user/'.$user.'/profile?apikey=85f0ab22-ac09-410f-82c4-99cd973db392');
    curl_setopt($ch, CURLOPT_GET, 1);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    $res = curl_exec($ch);

    echo json_encode($res);
?>