/*global Highcharts, $ */

/* 
 * Sally Demo
 *
 * Version: 0.1
 *
 * Author: Alex Cowell ( alxcwll [at] gmail [dot] com )
 * Author: Enrico Candino ( enrico.candino [at] gmail [dot] com )
 */
var Beancounter = window.Beancounter || {};

/*
 * A base set of options for the charts.
 * See the Highchart API for more details (http://www.highcharts.com/ref)
 *
 * You should configure the following options as necessary:
 *      chart.renderTo [String|Object]
 *      title.text [String]
 *      series [Object]
 */
Beancounter.getDefaultChartOptions = function () {
    return {
        chart: {
            renderTo: null,
            plotBackgroundColor: null,
            plotBorderWidth: null,
            plotShadow: false,
            events: {}
        },
        title: {
            text: null
        },
        tooltip: {
            formatter: function () {
                return '<b>' + this.point.name + '</b>: '
                    + Highcharts.numberFormat(this.percentage, 1) + ' %';
            }
        },
        plotOptions: {
            pie: {
                allowPointSelect: true,
                cursor: 'pointer',
                dataLabels: {
                    enabled: true,
                    color: '#000000',
                    connectorColor: '#000000',
                    formatter: function () {
                        return '<b>' + this.point.name + '</b>: '
                            + Highcharts.numberFormat(this.percentage, 1) + ' %';
                    }
                }
            },
            area: {
                stacking: 'percent',
                lineColor: '#ffffff',
                lineWidth: 1,
                marker: {
                    lineWidth: 1,
                    lineColor: '#ffffff'
                }
            }
        }
    };
};

// TODO: Change colours.
Highcharts.setOptions({
    colors: ['#326F93', '#385A6E', '#104260', '#66A4C9', '#80AEC9',
             '#326F93', '#385A6E', '#104260', '#66A4C9', '#80AEC9',
             '#326F93', '#385A6E', '#104260', '#66A4C9', '#80AEC9',
             '#326F93', '#385A6E', '#104260', '#66A4C9', '#80AEC9']
});

var testData = {
    "object": {
        "userId": "c5e1d997-a9fb-4488-8764-6e08269409c6",
        "visibility": "PUBLIC",
        "interests": [
            {
                "resource": "http://dbpedia.org/resource/Selfridges",
                "activities": [
                    "fd4f6fbf-13c2-4de7-82ca-ffd436405d9c"
                ],
                "visible": true,
                "weight": 0.02095305737012103
            },
            {
                "resource": "http://dbpedia.org/resource/Chance_%28band%29",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Amsterdam",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Twitter",
                "activities": [
                    "ad46b8ba-f673-4684-acd3-6c9e8f0e12c6",
                    "24bac05e-8f31-404c-97ab-dcb61da89dc7",
                    "6c052457-99a5-4a60-a8b2-977bb807d5f8",
                    "95bdc8ba-5b13-4d67-9134-4397ee17cdb5",
                    "a22586fe-e2cf-475a-bf69-6a8e753fe4c6"
                ],
                "visible": true,
                "weight": 0.004423006504690251
            },
            {
                "resource": "http://dbpedia.org/resource/Children_%28film%29",
                "activities": [
                    "5220c442-0a54-4dfb-8543-196197cfbb58",
                    "95bdc8ba-5b13-4d67-9134-4397ee17cdb5",
                    "86d146e9-f43e-4e4c-9e7d-1b7b11ef876f",
                    "a22586fe-e2cf-475a-bf69-6a8e753fe4c6",
                    "4ce2bc60-1457-4d5b-81c6-1e81e4e8aa06",
                    "43b48e80-2237-47a2-9983-17c90518dfff",
                    "b68e1d45-f1f3-48ba-9d7e-3a9a5fa77205"
                ],
                "visible": true,
                "weight": 0.01893951342614978
            },
            {
                "resource": "http://dbpedia.org/resource/Made_%28Netherlands%29",
                "activities": [
                    "56f89497-c0df-4bda-b575-0ae4d2120795",
                    "3a721d85-46ee-49ed-8ebe-519a1e87a294",
                    "c2b67d75-cb55-4a9e-b124-f6c9bd2e54fc"
                ],
                "visible": true,
                "weight": 0.012827952899143702
            },
            {
                "resource": "http://dbpedia.org/resource/Republic_%28political_organisation%29",
                "activities": [
                    "5220c442-0a54-4dfb-8543-196197cfbb58",
                    "95bdc8ba-5b13-4d67-9134-4397ee17cdb5",
                    "86d146e9-f43e-4e4c-9e7d-1b7b11ef876f",
                    "a22586fe-e2cf-475a-bf69-6a8e753fe4c6",
                    "4ce2bc60-1457-4d5b-81c6-1e81e4e8aa06",
                    "43b48e80-2237-47a2-9983-17c90518dfff",
                    "b68e1d45-f1f3-48ba-9d7e-3a9a5fa77205"
                ],
                "visible": true,
                "weight": 0.01893951342614978
            },
            {
                "resource": "http://dbpedia.org/resource/Shopping_%28film%29",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Free_%28rapper%29",
                "activities": [
                    "1beded03-c394-4784-8407-89eff638c3c3"
                ],
                "visible": true,
                "weight": 0.005091273285348852
            },
            {
                "resource": "http://dbpedia.org/resource/Lizzie_Borden",
                "activities": [
                    "973e411d-45ab-48b4-908b-8b2f03543493"
                ],
                "visible": true,
                "weight": 0.0325794120465657
            },
            {
                "resource": "http://dbpedia.org/resource/Rihanna",
                "activities": [
                    "24bac05e-8f31-404c-97ab-dcb61da89dc7",
                    "6c052457-99a5-4a60-a8b2-977bb807d5f8",
                    "cd84a32a-276b-4995-b09b-7030ba9c0ca8"
                ],
                "visible": true,
                "weight": 0.008578948533907924
            },
            {
                "resource": "http://dbpedia.org/resource/London_College_of_Fashion",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Birmingham",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Nick_Hornby",
                "activities": [
                    "86d146e9-f43e-4e4c-9e7d-1b7b11ef876f",
                    "4ce2bc60-1457-4d5b-81c6-1e81e4e8aa06",
                    "b68e1d45-f1f3-48ba-9d7e-3a9a5fa77205"
                ],
                "visible": true,
                "weight": 0.010147895780424054
            },
            {
                "resource": "http://dbpedia.org/resource/London",
                "activities": [
                    "52ec6d32-9d71-46e1-86e6-1f8ee3853af9",
                    "ffd36273-d331-4083-a9fd-e51c16db40a1",
                    "39287dc6-c880-4d3d-be87-345d18eed611",
                    "d2a48dcd-d496-4529-ad69-2a6c8c623572",
                    "24bac05e-8f31-404c-97ab-dcb61da89dc7",
                    "6c052457-99a5-4a60-a8b2-977bb807d5f8",
                    "fc71d59d-4192-494d-9899-307d7adf083a",
                    "0e0348d4-9ed3-4dcc-8499-ce30efc5cf95",
                    "84dc2e39-171d-4a63-945d-dd2a3452f0b5",
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be",
                    "2eee4fc0-61cb-4a4e-9bd5-efdd2b10c444"
                ],
                "visible": true,
                "weight": 0.09168837567265967
            },
            {
                "resource": "http://dbpedia.org/resource/Bells_%28Blackadder%29",
                "activities": [
                    "b11bffd3-9855-4013-b78a-ccb3888a8851"
                ],
                "visible": true,
                "weight": 0.005851658281267652
            },
            {
                "resource": "http://dbpedia.org/resource/Benjamin_Reynolds",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            },
            {
                "resource": "http://dbpedia.org/resource/Shoreditch",
                "activities": [
                    "073c377b-a1c0-44d0-905b-b60bc7444d03",
                    "3eef2837-e3f8-442f-98f1-c76df17b3842",
                    "b549d571-ae45-4739-bd99-409beb251bc3",
                    "5cab319e-7e8a-489a-ab63-78cd1b165eeb",
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be",
                    "db662b6b-25cb-432a-8472-9f3bcdeb9c79",
                    "6231f711-f390-45cf-b535-2e5b02ba9029",
                    "ccf69264-aa30-4c16-b6a6-ea1c8e80ed65",
                    "64019ebe-58d2-46ba-8ca7-51c62e3a9f21",
                    "bc279cd0-70e7-49cb-8f8b-a542b5fbec60",
                    "01a24ed8-bb0f-473d-98a0-0ea688a72991"
                ],
                "visible": true,
                "weight": 0.7330409285898609
            },
            {
                "resource": "http://dbpedia.org/resource/Perfect_%28musician%29",
                "activities": [
                    "6edcf276-5a27-4d0c-afb2-aedcdf8ea491"
                ],
                "visible": true,
                "weight": 0.003248144000747745
            },
            {
                "resource": "http://dbpedia.org/resource/Taiwan",
                "activities": [
                    "1fb6dda4-25ec-4503-ab0a-443e7fdb15be"
                ],
                "visible": true,
                "weight": 0.00481290288328039
            }
        ]
    },
    "message": "profile for user [sally-beancounter] found",
    "status": "OK"
};

Beancounter.Pie = function (username, container) {
    var chart,
        options = Beancounter.getDefaultChartOptions();

    function getNameFromResource(resource) {
        return resource.replace(/_/g, " ")
                .replace(/http:\/\/dbpedia\.org\/resource\/|\%28|\%29/gi, "");
    }

    function updateChartData(profile) {
        var i,
            len,
            interest,
            interests = profile.object.interests,
            name,
            point;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = getNameFromResource(interest.resource);
            point = chart.get(name);

            if (point !== undefined && point !== null) {
                point.update(interest.weight);
            } else {
                chart.get('user-profile').addPoint({
                    name: name,
                    id: name,
                    y: interest.weight
                }, false);
            }
        }

        chart.redraw();
    }

    function updateChart() {
        $.ajax({
            url: 'http://46.4.89.183/sally/profile-proxy.php',
            data: {
                'username': username
            },
            dataType: 'json',
            cache: false,
            success: function (data) {
                updateChartData($.parseJSON(data));
                setTimeout(updateChart, 5000);
            },
            error: function (request, errorText, data) {
                var obj = $.parseJSON(request.responseText),
                    error = obj.message;
                $("#errorContainer").html('<p>' + error + '</p>');
            }
        });
    }

    options.chart.renderTo = container;
    options.series = [{
        type: 'pie',
        id: 'user-profile',
        name: 'User Profile',
        data: []
    }];

    return {
        init: function () {
            chart = new Highcharts.Chart(options);
            updateChart();
        },

        getChart: function () {
            return chart;
        },

        getUsername: function () {
            return username;
        }
    };
};

Beancounter.Area = function (username, container) {
    var chart,
        options = Beancounter.getDefaultChartOptions();

    function getNameFromResource(resource) {
        return resource.replace(/_/g, " ")
                .replace(/http:\/\/dbpedia\.org\/resource\/|\%28|\%29/gi, "");
    }

    function updateChartData(profile) {
        var i,
            len,
            interest,
            interests = profile.object.interests,
            time = new Date().getTime(),
            name,
            series,
            doShift;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = getNameFromResource(interest.resource);
            series = chart.get(name);

            if (series === undefined || series === null) {
                chart.addSeries({
                    id: name,
                    name: name,
                    data: []
                });
            }

            series = chart.get(name);
            doShift = series.data.length > 20;

            series.addPoint({
                name: name,
                x: time,
                y: interest.weight
            }, false, doShift);
        }

        chart.redraw();
    }

    function updateChart() {
        $.ajax({
            url: 'http://46.4.89.183/sally/profile-proxy.php',
            data: {
                'username': username
            },
            dataType: 'json',
            cache: false,
            success: function (data) {
                updateChartData($.parseJSON(data));
                setTimeout(updateChart, 5000);
            },
            error: function (request, errorText, data) {
                var obj = $.parseJSON(request.responseText),
                    error = obj.message;
                $("#errorContainer").html('<p>' + error + '</p>');
            }
        });
    }

    options.chart.renderTo = container;
    options.chart.type = 'area';
    options.xAxis = {
        type: 'datetime',
        tickPixelInterval: 150,
        maxZoom: 20 * 1000
    };
    options.yAxis = {
        title: {
            text: 'Percent'
        }
    };
    options.series = [];

    return {
        init: function () {
            chart = new Highcharts.Chart(options);
            updateChart();
        },

        getChart: function () {
            return chart;
        },

        getUsername: function () {
            return username;
        }
    };
};

Beancounter.TagCloud = function (username, container) {
    function getNameFromResource(resource) {
        return resource.replace(/_/g, " ")
                .replace(/http:\/\/dbpedia\.org\/resource\/|\%28|\%29/gi, "");
    }

    function updateTagCloudData(profile) {
        var i,
            len,
            name,
            interestArray = [],
            interest,
            interests = profile.object.interests;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = getNameFromResource(interest.resource);
            interestArray.push({
                text: name,
                weight: interest.weight
            });
        }

        $('#' + container).empty().jQCloud(interestArray);
    }

    function updateTagCloud() {
        $.ajax({
            url: 'http://46.4.89.183/sally/profile-proxy.php',
            data: {
                'username': username
            },
            dataType: 'json',
            cache: false,
            success: function (data) {
                updateTagCloudData(testData);
                setTimeout(updateTagCloud, 5000);
            },
            error: function (request, errorText, data) {
                var obj = $.parseJSON(request.responseText),
                    error = obj.message;
                $("#errorContainer").html('<p>' + error + '</p>');
            }
        });
    }

    return {
        init: function () {
            updateTagCloud();
        }
    };
};
