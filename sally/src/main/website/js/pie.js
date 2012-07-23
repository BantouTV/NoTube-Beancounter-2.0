var Beancounter = Beancounter || {};

$(document).ready(function () {

    Beancounter.pie = Beancounter.pie || {};

    Beancounter.pie.start = function () {

        var firstTime = true;

        var graphPie = function () {

            var chart;

            var buildGraph = function (data) {

                chart = new Highcharts.Chart({
                    chart: {
                        renderTo: 'container',
                        plotBackgroundColor: null,
                        plotBorderWidth: null,
                        plotShadow: false
                    },
                    title: {
                        text: 'Sally\'s profile'
                    },
                    tooltip: {
                        formatter: function () {
                            return '<b>' + this.point.name + '</b>: ' + this.percentage + ' %';
                        }
                    },
                    plotOptions: {
                        pie: {
                            animation: firstTime,
                            allowPointSelect: true,
                            cursor: 'pointer',
                            dataLabels: {
                                enabled: true,
                                color: '#000000',
                                connectorColor: '#000000',
                                formatter: function () {
                                    return '<b>' + this.point.name + '</b>: ' + this.percentage + ' %';
                                }
                            }
                        }
                    },
                    series: [{
                        type: 'pie',
                        name: 'Browser share',
                        data: data
                    }]
                });
            };


            $.ajax({
                url: 'http://api.beancounter.io/rest/user/sally-beancounter/profile/pie?apikey=85f0ab22-ac09-410f-82c4-99cd973db392',
                dataType: 'json',
                success: function (data, textstatus, request) {
                    var obj = $.parseJSON(request.responseText)
                    console.log(obj.object);
                    var data = obj.object;
                    data = data.sort( function(a, b) {
                        if (a[1] > b[1]) return -1;
                        if (a[1] < b[1]) return 1;
                        if (a[0] < b[0]) return 1;
                    });
                    for(var val in data) {
                        data[val][0] = data[val][0].replace("http://dbpedia.org/resource/","");
                        data[val][0] = data[val][0].replace("_%28"," ");
                        data[val][0] = data[val][0].replace("%29","");
                        data[val][0] = data[val][0].replace("_"," ");
                    }
                    buildGraph(data);
                    firstTime = false;
                },
                error: function (request, error, data) {
                    var obj = $.parseJSON(request.responseText);
                    var error = obj.message;
                    $("#errorContainer").html('<p>'+error+'</p>');
                }
            });

        };

        graphPie();

        setInterval(graphPie, 2000);
    };
});
