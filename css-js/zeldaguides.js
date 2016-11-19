jQuery(document).ready(function(){

    $.fn.hasPartialClass = function(partial){
      return new RegExp(partial).test(this.prop('class'));
    };

	var checkedIDs = simpleStorage.get('checkedIDs') || [];
	var gameVariation = simpleStorage.get('gameVariation') || $("#zeldaguide-gameSelector option:first").val();

	// transfer cookies to storage
	if (typeof Cookies !== 'undefined') {
        var checkedIDs_cookie = Cookies.getJSON('checkedIDs');
        if (checkedIDs_cookie !== undefined) {
            console.log("Cookie detected. Transferring checkmarks state from cookie to local storage and deleting cookie.");
            checkedIDs = checkedIDs_cookie;
            simpleStorage.set('checkedIDs', checkedIDs);
            Cookies.remove('checkedIDs', { path: '' });
        }
    }

	// update checkboxes based on saved data (clear all, and check the ones in local storage)
	$("#zeldaguide input[type='checkbox']:enabled").each(function() {
		this.checked = false;
	});
	for	(var i in checkedIDs) {
		$('#' + checkedIDs[i]).prop('checked', true);
	}

	// make sure all disabled boxes are checked
	$("#zeldaguide input[type='checkbox']:disabled").each(function() {
    		this.checked = true;
    });

    // update game variation based on value in local storage
	$("#zeldaguide-gameSelector").val(gameVariation);

	// function to hide filtered items
	function hideFiltered() {
        // un-hide all filtered items (only those not governed by game variations)
        $(".zeldaguide-filtered").each(function() {
            if ($(this).hasPartialClass("zeldaguide-gameVariation-")) return;
            $(this).show();
        });
        // then go though all filter controls and hide checked filters
        $("#zeldaguide-filtercontrols input").each(function() {
            var filterID = $(this).prop("name");
            if (this.checked) {
                $(".zeldaguide-filter-" + filterID).hide();
            }
        });
        // finally go though all filter controls and show unchecked filters
        // (this is needed so that with items containing several filters are shown
        // if only one of the filters are selected)
        $("#zeldaguide-filtercontrols input").each(function() {
            var filterID = $(this).prop("name");
            if (!this.checked) {
                $(".zeldaguide-filtered").each(function() {
                    if ($(this).hasPartialClass("zeldaguide-gameVariation-")
                        && !$(this).hasClass("zeldaGuide-gameVariation-" + $("#zeldaguide-gameSelector").val())) {
                        return;
                    }
                    if ($(this).hasClass("zeldaguide-filter-" + filterID)) $(this).show();
                });
            }
        });
    }

	// function to call when a checkbox is changed
	function saveChecked_simpleStorage() {
		var id = $(this).attr('id')
		if (this.checked && checkedIDs.indexOf(id) == -1) {
			checkedIDs.push(id);
		} else if (!this.checked && checkedIDs.indexOf(id) != -1) {
			checkedIDs.splice(checkedIDs.indexOf(id), 1);
		}
		simpleStorage.set('checkedIDs', checkedIDs);
	}

	function saveGameSelector() {
	    simpleStorage.set('gameVariation', $(this).val());
	}

	function checkAll() {
	    if (confirm('Are you sure you want to check everything?')) {
            $(".zeldaguide-gameSegment input[type='checkbox']:enabled").prop('checked', true).change();
        }
	}

	function uncheckAll() {
	    if (confirm('Are you sure you want to uncheck everything?')) {
            $(".zeldaguide-gameSegment input[type='checkbox']:enabled").prop('checked', false).change();
        }
	}

	function updateJumpLinks() {
	    // get ID of first unchecked checkbox and last checked checkbox
	    // (only including visible entries and enabled checkboxes)
	    var firstUncheckedID = $(".zeldaguide-gameSegment li:visible input[type='checkbox']:enabled:not(:checked)")
	            .first().closest("li").attr("id");
	    var lastCheckedID = $(".zeldaguide-gameSegment li:visible input[type='checkbox']:enabled:checked")
	            .last().closest("li").attr("id");
        // update the jump links to these IDs
	    $("#zeldaguide-jumpFirstUnchecked").attr("href", firstUncheckedID ? "#" + firstUncheckedID : "javascript:void(0);");
	    $("#zeldaguide-jumpLastChecked").attr("href", lastCheckedID ? "#" + lastCheckedID : "javascript:void(0);");
	}

	function gameSelectorUpdateDescriptions() {
        // hide all game variation descriptions
        $("#zeldaguide-gameSelector option").each(function() {
            var thisClass = "zeldaguide-gameVariation-" + $(this).val();
            $('.' + thisClass).hide();
        });
        // show 'all' and the selected game variation
        $(".zeldaguide-gameVariation-all").show();
        $('.' + "zeldaguide-gameVariation-" + $(this).val()).show();
        // make sure filtered items are still hidden
        hideFiltered();
	}

	// each time a checkbox changes, update local storage
	$("#zeldaguide input[type='checkbox']:enabled").change(saveChecked_simpleStorage);
	// each time the game selector changes, update local storage
    $("#zeldaguide-gameSelector").change(saveGameSelector);
    // each time the game selector changes, update descriptions
    $("#zeldaguide-gameSelector").change(gameSelectorUpdateDescriptions);

    // check/uncheck all buttons
    $('#zeldaguide-uncheckAll').click(uncheckAll);
    $('#zeldaguide-checkAll').click(checkAll);

    // update "jump to item" links when items and settings are checked/unchecked
    // and when game selector is changed
    $("#zeldaguide input[type='checkbox']:enabled").change(updateJumpLinks);
    $("#zeldaguide-gameSelector").change(updateJumpLinks);

    // each time a filter control changes, hide filtered items
    $("#zeldaguide-filtercontrols input").change(hideFiltered);

    // trigger change on game selector to show correct descriptions/items
    $("#zeldaguide-gameSelector").change();

    // trigger change on all settings checkboxes
	$(".zeldaguide-settings input[type='checkbox']").change();

    // make sure we're at the right place (things may have moved around due to showing/hiding elements
    if (window.location.hash) {
        window.location.hash = window.location.hash;
    }


});

