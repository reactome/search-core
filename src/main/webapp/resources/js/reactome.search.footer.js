$(document).ready(function() {
    $('#local-searchbox').autocomplete({
        serviceUrl: '/content/getTags',
        minChars:2,
        deferRequestBy: 250,
        paramName: "tagName",
        delimiter: ",",
        transformResult: function(response) {
            return {
                suggestions: $.map($.parseJSON(response), function(item) {
                    return { value: item };
                })
            };
        },
        onSelect: function(value, data){$("#search_form").submit()}
    });


});

$('ul.term-list').each(function(){

    var LiN = $(this).find('li').length;

    if( LiN > 6){
        $('li', this).eq(5).nextAll().hide().addClass('toggleable');
        $(this).append('<li class="more">More...</li>');
    }

});


$('ul.term-list').on('click','.more', function(){

    if( $(this).hasClass('less') ){
        $(this).text('More...').removeClass('less');
    }else{
        $(this).text('Less...').addClass('less');
    }

    $(this).siblings('li.toggleable').slideToggle();

});

$('#search_form').submit(function(e) {
    if (!$('#local-searchbox').val()) {
        e.preventDefault();
    } else if ($('#local-searchbox').val().match(/^\s*$/)){
        e.preventDefault();
    }
});

$(".plus").click(function () {
    $plus = $(this);
    console.debug($(this));
    $treeContent = $plus.nextAll().eq(1);
    $treeContent.slideToggle(500, function () {
        if ($treeContent.is(":visible") ){
            return $plus.find(".image").attr("src", "../resources/images/minus.png");
        } else {
            return $plus.find(".image").attr("src", "../resources/images/plus.png");
        }
    });
});

$('#availableSpeciesSel').ready(function() {
    var DEFAULT_SPECIES = 'Homo sapiens';

    /** Check if hash is present in the URL **/
    var hash = decodeURIComponent(window.location.hash);
    var defaulLoaded = false;
    if(hash == "") {
        $("div[class*=tplSpe_]").each(function (index, value) {
            var item = $(value).attr("class");
            if (item == "tplSpe_" + DEFAULT_SPECIES.replace(" ", "_")) {
                $("#availableSpeciesSel").val(DEFAULT_SPECIES.replace(" ", "_"));
                $("." + item).show();

                //change url
                if($("#availableSpeciesSel").val() != null) {
                    window.location.hash = "#" + encodeURIComponent(DEFAULT_SPECIES);
                }

                defaulLoaded = true;
            }else {
                $("." + item).css("display", "none");
            }
        });

        if(!defaulLoaded){
            $("div[class*=tplSpe_]").css("display", "block");
        }
    }else {
        hash = hash.replace("#", "").replace(" ", "_");

        // hash has been change manually into a non-existing value. Pick the first one which is human
        if ($(".tplSpe_" + hash).val() == null) {

            $("#availableSpeciesSel > option").each(function (index, value) {
                var item = $(value).attr("value");

                $("#availableSpeciesSel").val(item);

                $(".tplSpe_" + item).show();
                window.location.hash = "#" + encodeURIComponent(item.replace("_", " "));

                return false;
            });
        } else {
            $("#availableSpeciesSel").val(hash);
            $(".tplSpe_" + hash).show();
        }
    }
});

$('#availableSpeciesSel').on('change', function() {
    var selectedSpecies = this.value;

    // hide everything
    $("div[class*=tplSpe_]").each(function( index, element ){
        $(element).hide();
    });

    // show div related to the species
    $(".tplSpe_" + selectedSpecies).show();

    // change anchor in the URL
    window.location.hash = "#"+encodeURIComponent(selectedSpecies.replace("_", " "));

});