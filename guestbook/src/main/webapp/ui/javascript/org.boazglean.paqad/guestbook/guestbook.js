$(document).ready(function() {

	function ConvertFormToJSON(form){
		var array = jQuery(form).serializeArray();
		var json = {};

		jQuery.each(array, function() {
			json[this.name] = this.value || '';
		});

		return json;
	}

	console.log($.url().param('guestbook'));

    $('.guestbookname').text($.url().param('guestbook'));
    $('input[name="guestbook"]').val($.url().param('guestbook'));
    $('input[name="host"]').val($.url().param('host'));
    $('textarea[name="body"]').val('');

	var signedInRender = $('.signedin').compile({
        '.nickname': 'nickname',
        '.signout@href': function(arg) {
       		return arg.context.signOutUrl.replace("placeholder", encodeURIComponent(location.href))
        },
        '.@style': function(arg) {
        	if(arg.context.signOutUrl == null) {
        		return 'display:none;';
        	}
        	else return '';
        }
	});

	var signedOutRender = $('.signedout').compile({
        '.signin@href': function(arg) {
       		return arg.context.signInUrl.replace("placeholder", encodeURIComponent(location.href))
        },
		'.@style': function(arg) {
        	if(arg.context.signInUrl == null) {
        		return 'display:none;';
        	}
        	else return '';
		}
	})

	var postingRender = $('.postings').compile({
		'.empty@style': function(arg) {
        	if(arg.context.postings.length != 0) {
        		return 'display:none;';
        	}
        	else return '';
		},
		'.many@style': function(arg) {
        	if(arg.context.postings.length == 0) {
        		return 'display:none;';
        	}
        	else return '';
		},
		'.posting': { 'posting<-postings': {
            	'.author@style': function(arg) {
					if(arg.context.postings[arg.pos].author == null || arg.context.postings[arg.pos].author == 0) {
						return 'display:none;';
					}
					else return '';
				},
            	'.anonymous-author@style': function(arg) {
					if(!(arg.context.postings[arg.pos].author == null || arg.context.postings[arg.pos].author == 0)) {
						return 'display:none;';
					}
					else return '';
				},
            	'.authorname' : 'posting.author',
				'.body': 'posting.body',
			}
		}
	});

    $(document).on("/user", function(event, data) {
    	$('.signedin').replaceWith(signedInRender(data));
    	$('.signedout').replaceWith(signedOutRender(data));
    });
    $(document).on("/posting", function(event, data) {
    	$('.postings').replaceWith(postingRender(data));
    });


	$.ajax("../api/user");
	$.ajax("../api/posting" + window.location.search);

	$('#guestbook-form').bind('submit', function(event) {
    	event.preventDefault();
    	var json = ConvertFormToJSON(this);

		$.ajax({
			type: "POST",
			url: "../api/posting",
			data: JSON.stringify(json),
			contentType: 'application/json' ,
			dataType: "json",
		}).always(function() {
			$.ajax("../api/posting" + window.location.search);
		});

	});




});