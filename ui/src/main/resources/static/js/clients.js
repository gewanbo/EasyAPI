var colors = {
    'a':         '#edbd00',
    'social':              '#367d85',
    'animals':             '#97ba4c',
    'health':              '#f5662b',
    'research_ingredient': '#3f3e47',
    'fallback':            '#9f9fa3'
};
d3.json("/clients/data", function(error, json) {
    var chart = d3.select("#chart").append("svg").chart("Sankey.Path");
    chart.name(label).colorNodes(function(name, node) {
        return color(node, 1) || colors.fallback;
    }).colorLinks(function(link) {
        return color(link.source, 4) || color(link.target, 1) || colors.fallback;
    })
    .nodeWidth(15)
    .nodePadding(10)
    .spread(true)
    .iterations(0)
    .draw(json);

    function label(node) {
        return node.name.replace(/\s*\(.*?\)$/, '');
    }

    function color(node, depth) {
        var id = node.id.replace(/(_score)?(_\d+)?$/, '');
        if (colors[id]) {
            return colors[id];
        } else if (depth > 0 && node.targetLinks && node.targetLinks.length == 1) {
            return color(node.targetLinks[0].source, depth-1);
        } else {
            return null;
        }
    }
});