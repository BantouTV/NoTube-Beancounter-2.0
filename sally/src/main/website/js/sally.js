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
        defaultOuterRadius = r - 20,
        hoverOuterRadius = r,
        data = [],
        selectedArc,
        colour = d3.scale.category20b(),
        random = d3.random.normal(0, 1),
        pie = d3.layout.pie().sort(null).value(function (d) { return d.weight; }),
        arc = d3.svg.arc().innerRadius(r - 150).outerRadius(defaultOuterRadius),
        arcHover = d3.svg.arc().innerRadius(r - 150).outerRadius(hoverOuterRadius),
        svg = d3.select(container)
            .append("svg")
                .attr("width", w)
                .attr("height", h)
            .append("g")
                .attr("transform", "translate(" + w / 2 + "," + h / 2 + ")"),
        pieStats = svg.append("g").attr("class", "pie-stats"),
        pieStatsInterest = pieStats.append("text")
            .attr("class", "pie-stats-interest")
            .attr("text-anchor", "middle")
            .attr("dy", -10),
        pieStatsWeight = pieStats.append("text")
            .attr("class", "pie-stats-weight")
            .attr("text-anchor", "middle")
            .attr("dy", 10);

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

    function arcTween(a) {
        var i = d3.interpolate(this.currentArc, a),
            arcUpdate = d3.svg.arc().innerRadius(r - 150);
        this.currentArc = i(0);
        arcUpdate.outerRadius(this.outerRadius === undefined
            ? defaultOuterRadius
            : this.outerRadius);

        return function (t) {
            return arcUpdate(i(t));
        };
    }

    function handleClickOnArc(d) {
        if (selectedArc !== undefined && selectedArc !== null) {
            selectedArc
                .classed("selected", false)
                .transition()
                .attr("d", arc)
                .each(function (d) {
                    this.outerRadius = defaultOuterRadius;
                    this.currentArc = d;
                });
        }

        selectedArc = d3.select(this);
        selectedArc.classed("selected", true)
            .transition()
            .attr("d", arcHover)
            .each(function (d) {
                this.outerRadius = hoverOuterRadius;
                this.currentArc = d;
            });

        pieStatsInterest.text(d.data.name);
        pieStatsWeight.text(d.value + "%");
    }

    function redraw() {
        var arcs, arcsEnter, arcsExit,
            currentWeight;

        // TODO: Replace this with real data from AJAX call.
        data = getData();
        arcs = svg.selectAll("g.arc")
            .data(pie(data), function (d) { return d.data.name; });

        arcsEnter = arcs.enter().append("g")
            .attr("class", "arc");

        arcsEnter.append("path")
            .attr("fill", function (d, i) { return colour(i); })
            .attr("d", arc)
            .on("click", handleClickOnArc)
            .each(function (d) { this.currentArc = d; });

        arcs.select("path").transition().duration(750).attrTween("d", arcTween);
        if (selectedArc !== undefined && selectedArc !== null) {
            selectedArc.each(function (d) { currentWeight = d.value; });
            pieStatsWeight.text(currentWeight + "%");
        }

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
        stack = d3.layout.stack().offset("silhouette")
            .values(function (d) { return d.values; })
            .x(function (d, i) { return i; }),
        data = [],
        colour = d3.scale.category20b(),
        random = d3.random.normal(0, 0.2),
        width = 960,
        height = 500,
        mx = n - 1,
        my = 100,
        x = d3.scale.linear().range([0, width]),
        y = d3.scale.linear().range([height, 0]),
        area = d3.svg.area()
            .x(function (d, i) { return x(i); })
            .y0(function (d) { return y(d.y0); })
            .y1(function (d) { return y(d.y + d.y0); }),
        svg = d3.select(container)
            .append("svg")
                .attr("width", width)
                .attr("height", height);

    svg.append("defs").append("clipPath")
            .attr("id", "clip")
        .append("rect")
            .attr("width", width - 10)
            .attr("height", height);

    svg = svg.append("g")
        .attr("class", "stream-graph")
        .attr("clip-path", "url(#clip)");

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
        y.domain([0, d3.max(stack(data), function (d) {
            return d3.max(d.values, function (d) {
                return d.y0 + d.y;
            });
        })]);

        layers = svg.selectAll("path").data(stack(data));

        layers.enter().append("path")
            .style("fill", function (d, i) { return colour(i); })
            .attr("d", function (d) { return area(d.values); })
            .on("click", function (d) {
                // TODO.
            })
            .append("title")
                .text(function (d) { return d.name; });

        // TODO: Fix the jerky re-centring of the graph in the y-axis.
        // Draw the new data outside the range of the x-axis, then update the
        // domain of the scale and gracefully transition the new data into the
        // container.
        // layers.transition()
        //     .duration(500)
        //     .attr("d", function (d) { return area(d.values); });
        layers.attr("d", function (d) { return area(d.values); });
        x.domain([0, (data[0].values.length > n) ? n : data[0].values.length - 1]);
        layers.transition()
            .duration(1000)
            .attr("d", function (d) { return area(d.values); });
        // layers.transition()
        //     .delay(500)
        //     .duration(500)
        //     .attr("d", function (d) {
        //         x.domain([0, (data[0].values.length > n) ? n : data[0].values.length - 1]);
        //         return area(d.values);
        //     });

        // If the number of samples has reached some specified threshold, start
        // scrolling the data across the container.
        if (data[0].values.length > n) {
            layers.attr("transform", null)
                .transition()
                    .duration(1000)
                    .ease("linear")
                    .attr("transform", "translate(" + x(-1) + ")");

            data = removeData(data);
        }
    }

    function updateStreamGraph() {
        redraw();
        setTimeout(updateStreamGraph, 1000);
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
