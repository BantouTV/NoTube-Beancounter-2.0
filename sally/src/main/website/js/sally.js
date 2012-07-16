/*global $, d3, testData */

/* 
 * Sally Demo
 *
 * Version: 0.2
 *
 * Author: Alex Cowell ( alxcwll [at] gmail [dot] com )
 * Author: Enrico Candino ( enrico.candino [at] gmail [dot] com )
 */
var Beancounter = window.Beancounter || {};

Beancounter.getNameFromResource = function (resource) {
    return resource.replace(/_/g, " ")
            .replace(/http:\/\/dbpedia\.org\/resource\/|\%28|\%29/gi, "");
};

Beancounter.Pie = function (username, container) {
    var w = 960,
        h = 500,
        r = Math.min(w, h) / 2,
        data = [],
        colour = d3.scale.category20b(),
        random = d3.random.normal(0, 1),
        pie = d3.layout.pie().sort(null).value(function (d) { return d.weight; }),
        arc = d3.svg.arc().innerRadius(r - 150).outerRadius(r - 20),
        svg = d3.select(container)
            .append("svg")
                .attr("width", w)
                .attr("height", h)
            .append("g")
                .attr("transform", "translate(" + w / 2 + "," + h / 2 + ")");

    function getData() {
        var i,
            len,
            name,
            interestArray = [],
            interest,
            interests = testData.object.interests;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = Beancounter.getNameFromResource(interest.resource);
            interestArray.push({
                'name': name,
                'weight': Math.abs((interest.weight * 10) + random())
            });
        }

        return interestArray;
    }

    // Store the currently-displayed angles in this.currentArc.
    // Then, interpolate from this.currentArc to the new angles.
    function arcTween(a) {
        var i = d3.interpolate(this.currentArc, a);
        this.currentArc = i(0);
        return function (t) {
            return arc(i(t));
        };
    }

    function redraw() {
        var arcs, arcsEnter, arcsUpdate, arcsExit;

        // TODO: Replace this with real data from AJAX call.
        data = getData();
        arcs = svg.selectAll("g.arc")
            .data(pie(data), function (d) { return d.data.name; });

        arcsEnter = arcs.enter().append("g")
            .attr("class", "arc");

        arcsEnter.append("path")
            .attr("fill", function (d, i) { return colour(i); })
            .attr("d", arc)
            .each(function (d) { this.currentArc = d; });

        arcs.select("path").transition().duration(750).attrTween("d", arcTween);

        // TODO: Handle interests being removed.
    }

    function updatePieChart() {
        redraw();
        setTimeout(updatePieChart, 2000);
    }

    return {
        init: function () {
            updatePieChart();
        }
    };
};

Beancounter.StreamGraph = function (username, container) {
    var n = 200,
        stack = d3.layout.stack().offset("wiggle")
            .values(function (d) { return d.values; })
            .x(function (d, i) { return i; }),
        data = [],
        colour = d3.scale.category20b(),
        random = d3.random.normal(0, 0.2),
        width = 960,
        height = 500,
        mx = n - 1,
        my = 100,
        // my = d3.max(data0, function (d) {
        //     return d3.max(d.values, function (d) {
        //         return d.y0 + d.y;
        //     });
        // }),
        x = d3.scale.linear().range([0, width]),
        area = d3.svg.area()
            .x(function (d, i) { return x(i); })
            .y0(function (d) { return height - d.y0 * height / my; })
            .y1(function (d) { return height - (d.y + d.y0) * height / my; }),
        svg = d3.select(container)
            .append("svg")
                .attr("width", width)
                .attr("height", height);

    svg.append("defs").append("clipPath")
            .attr("id", "clip")
        .append("rect")
            .attr("width", width - 10)
            .attr("height", height);

    svg = svg.append("g").attr("clip-path", "url(#clip)");

    function streamIndex(d) {
        return {y: Math.max(0, d)};
    }

    function getData() {
        var i,
            len,
            name,
            layers = [],
            interest,
            interests = testData.object.interests;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = Beancounter.getNameFromResource(interest.resource);
            layers.push({
                name: name,
                values: [streamIndex(interest.weight)]
            });
        }

        return layers;
    }

    function addData(data) {
        return data.map(function (d) {
            var currentValue = d.values[d.values.length - 1];
            d.values.push(streamIndex(currentValue.y + random()));
            return d;
        });
    }

    function removeData(data) {
        return data.map(function (d) {
            d.values.shift();
            return d;
        });
    }

    function redraw() {
        var layers;

        data = addData(data);
        x.domain([0, (data[0].values.length > n) ? n : data[0].values.length - 1]);

        layers = svg.selectAll("path").data(stack(data));

        layers.enter().append("path")
                .style("fill", function (d, i) { return colour(i); })
                .attr("d", function (d) { return area(d.values); })
            .append("title")
                .text(function (d) { return d.name; });

        /*
        if (data[0].values.length < n) {
            layers.transition()
                .duration(500)
                .attr("d", function (d) { return area(d.values); });
        } else {
            layers.attr("d", function (d) { return area(d.values); });
        }
        */

        layers.attr("d", function (d) { return area(d.values); });

        if (data[0].values.length > n) {
            layers.attr("transform", null)
                .transition()
                    .duration(5000)
                    .ease("linear")
                    .attr("transform", "translate(" + x(-1) + ")");

            data = removeData(data);
        }
    }

    function updateStreamGraph() {
        redraw();
        setTimeout(updateStreamGraph, 5000);
    }

    return {
        init: function () {
            data = getData();
            updateStreamGraph();
        }
    };
};

Beancounter.TagCloud = function (username, container) {

    function updateTagCloudData(profile) {
        var i,
            len,
            name,
            interestArray = [],
            interest,
            interests = profile.object.interests;

        for (i = 0, len = interests.length; i < len; i++) {
            interest = interests[i];
            name = Beancounter.getNameFromResource(interest.resource);
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
