$(function () {

    "use strict";

    // On window's load
    $(window).on('load', function () {
        setTimeout(function () {
            $(".page_loader").fadeOut("fast");
            //$('link[id="style_sheet"]').attr('href', 'assets/css/skins/default.css');
            setDefaultPosition();
        }, 1000);
        if ($('body .filter-portfolio').length > 0) {
            $(function () {
                $('.filter-portfolio').filterizr(
                    {
                        delay: 0
                    }
                );
            });
            $('.filteriz-navigation li').on('click', function () {
                $('.filteriz-navigation .filtr').removeClass('active');
                $(this).addClass('active');
            });
        }
    });

    var headerHeight = $('.main-header').height();

    if($('.sticky-header-scroll').length > 0) {
        var stickyOffset = $('.sticky-header-scroll').offset().top;
        $(window).scroll(function(){
            var sticky = $('.sticky-header-scroll'),
                scroll = $(window).scrollTop() + (headerHeight * 2);

            if (scroll >= stickyOffset) sticky.addClass('fixed');
            else sticky.removeClass('fixed');
        });
    }

    // Page scroll
   /* $('body').scrollspy({target: ".main-header", offset: 200});*/
    $("#navbar a.single").on('click', function(event) {
        $('#navbar').removeClass('in');
        $('.navbar-collapse').collapse('hide');
        var topDistance = headerHeight;
        if($(document).find('.abs-header').length > 0) {
            if($(document).find('.abs-header.fixed').length < 1) {
                topDistance = headerHeight * 2;
            }
        }

        if (this.hash !== "") {
            event.preventDefault();
            var hash = this.hash;
            $('html, body').animate({
                scrollTop: $(hash).offset().top - topDistance
            }, 800, function(){
                //window.location.hash = hash;
            });
        }
    });

    function setDefaultPosition() {
        var url = window.location.href;
        url = url.split('#');
        if(url.length == 2) {
            var selector = url[1];
            if($('#' + selector).length > 0) {
                $('html, body').animate({
                    scrollTop: $('#' + selector).offset().top - headerHeight
                }, 'slow');
            }
        }
    }

    // Header shrink while page scroll
    adjustHeader();
    doSticky();
    $(window).on('scroll', function () {
        adjustHeader();
        doSticky();
    });

    function adjustHeader()
    {
        if ($(document).scrollTop() >= 100) {
            if($('.header-shrink').length < 1) {
                $('.sticky-header').addClass('header-shrink');
            }
            if($('.do-sticky').length < 1) {
                $('.logo img').attr('src', 'images/logo.png');
            }
        } else {
            $('.sticky-header').removeClass('header-shrink');
            if($('.do-sticky').length < 1) {
                $('.logo img').attr('src', 'assets/img/logo.png');
            }
        }
    }

    function doSticky()
    {
        if ($(document).scrollTop() > 40) {
            $('.do-sticky').addClass('sticky-header');
            //$('.do-sticky').addClass('header-shrink');
        }
        else {
            $('.do-sticky').removeClass('sticky-header');
            //$('.do-sticky').removeClass('header-shrink');
        }
    }

    // WOW animation library initialization
    var wow = new WOW(
        {
            animateClass: 'animated',
            offset: 100,
            mobile: false
        }
    );
    wow.init();

    $(".open-offcanvas, .close-offcanvas").on("click", function () {
        return $("body").toggleClass("off-canvas-sidebar-open"), !1
    });

    $(document).on("click", function (t) {
        var a = $(".off-canvas-sidebar");
        a.is(t.target) || 0 !== a.has(t.target).length || $("body").removeClass("off-canvas-sidebar-open")
    });

    // Banner slider
    //Function to animate slider captions
    function doAnimations(elems) {
        //Cache the animationend event in a variable
        var animEndEv = 'webkitAnimationEnd animationend';
        elems.each(function () {
            var $this = $(this),
                $animationType = $this.data('animation');
            $this.addClass($animationType).one(animEndEv, function () {
                $this.removeClass($animationType);
            });
        });
    }

    //Variables on page load
    var $myCarousel = $('#carouselExampleIndicators');
    var $firstAnimatingElems = $myCarousel.find('.item:first').find("[data-animation ^= 'animated']");
    //Initialize carousel
    $myCarousel.carousel();

    //Animate captions in first slide on page load
    doAnimations($firstAnimatingElems);
    //Pause carousel
    $myCarousel.carousel('pause');
    //Other slides to be animated on carousel slide event
    $myCarousel.on('slide.bs.carousel', function (e) {
        var $animatingElems = $(e.relatedTarget).find("[data-animation ^= 'animated']");
        doAnimations($animatingElems);
    });
    $('#carouselExampleIndicators').carousel({
        interval: 3000,
        pause: "false"
    });

    // Megamenu activation
    $(".megamenu").on("click", function (e) {
        e.stopPropagation();
    });

    // DROPDOWN ON HOVER

   $(".dropdown").on('hover', function () {
            $('.dropdown-menu', this).stop().fadeIn("fast");
        },
        function () {
            $('.dropdown-menu', this).stop().fadeOut("fast");
        });

    // Slick Sliders
    $('.slick-carousel').each(function () {
        var slider = $(this);
        $(this).slick({
            infinite: true,
            dots: false,
            arrows: false,
            centerMode: true,
            centerPadding: '0'
        });

        $(this).closest('.slick-slider-area').find('.slick-prev').on("click", function () {
            slider.slick('slickPrev');
        });
        $(this).closest('.slick-slider-area').find('.slick-next').on("click", function () {
            slider.slick('slickNext');
        });
    });


    // Full  Page Search Activation
    $(function () {
        $('a[href="#full-page-search"]').on('click', function(event) {
            event.preventDefault();
            $('#full-page-search').addClass('open');
            $('#full-page-search > form > input[type="search"]').focus();
        });

        $('#full-page-search, #full-page-search button.close').on('click keyup', function(event) {
            if (event.target === this || event.target.className === 'close' || event.keyCode === 27) {
                $(this).removeClass('open');
            }
        });

        /*$('form').on('submit', function(event) {
            event.preventDefault();
            return false;
        })*/
    });




    // Page scroller initialization.
    $.scrollUp({
        scrollName: 'page_scroller',
        scrollDistance: 300,
        scrollFrom: 'top',
        scrollSpeed: 500,
        easingType: 'linear',
        animation: 'fade',
        animationSpeed: 200,
        scrollTrigger: false,
        scrollTarget: false,
        scrollText: '<i class="fa fa-chevron-up"></i>',
        scrollTitle: false,
        scrollImg: false,
        activeOverlay: false,
        zIndex: 2147483647
    });

    // Multi-item carousel activation
    var itemsMainDiv = ('.multi-carousel');
    var itemsDiv = ('.multi-carousel-inner');
    var itemWidth = "";

    $('.leftLst, .rightLst').on('click', function () {
        var condition = $(this).hasClass("leftLst");
        if (condition)
            click(0, this);
        else
            click(1, this)
    });
    ResCarouselSize();

    $(window).on('resize', function () {
        ResCarouselSize();
        resizeModalsContent();
        adjustHeader()
    });
    function ResCarouselSize() {
        var incno = 0;
        var dataItems = ("data-items");
        var itemClass = ('.item');
        var id = 0;
        var btnParentSb = '';
        var itemsSplit = '';
        var sampwidth = $(itemsMainDiv).width();
        var bodyWidth = $('body').width();
        $(itemsDiv).each(function () {
            id = id + 1;
            var itemNumbers = $(this).find(itemClass).length;
            btnParentSb = $(this).parent().attr(dataItems);
            itemsSplit = btnParentSb.split(',');
            $(this).parent().attr("id", "multiCarousel" + id);


            if (bodyWidth >= 1200) {
                incno = itemsSplit[3];
                itemWidth = sampwidth / incno;
            }
            else if (bodyWidth >= 992) {
                incno = itemsSplit[2];
                itemWidth = sampwidth / incno;
            }
            else if (bodyWidth >= 768) {
                incno = itemsSplit[1];
                itemWidth = sampwidth / incno;
            }
            else {
                incno = itemsSplit[0];
                itemWidth = sampwidth / incno;
            }
            $(this).css({ 'transform': 'translateX(0px)', 'width': itemWidth * itemNumbers });
            $(this).find(itemClass).each(function () {
                $(this).outerWidth(itemWidth);
            });

            $(".leftLst").addClass("over");
            $(".rightLst").removeClass("over");

        });
    }

    function ResCarousel(e, el, s) {
        var leftBtn = ('.leftLst');
        var rightBtn = ('.rightLst');
        var translateXval = '';
        var divStyle = $(el + ' ' + itemsDiv).css('transform');
        var values = divStyle.match(/-?[\d\.]+/g);
        var xds = Math.abs(values[4]);
        if (e === 0) {
            translateXval = parseInt(xds, 10) - parseInt(itemWidth * s, 10);
            $(el + ' ' + rightBtn).removeClass("over");

            if (translateXval <= itemWidth / 2) {
                translateXval = 0;
                $(el + ' ' + leftBtn).addClass("over");
            }
        }
        else if (e === 1) {
            var itemsCondition = $(el).find(itemsDiv).width() - $(el).width();
            translateXval = parseInt(xds, 10) + parseInt(itemWidth * s, 10);
            $(el + ' ' + leftBtn).removeClass("over");

            if (translateXval >= itemsCondition - itemWidth / 2) {
                translateXval = itemsCondition;
                $(el + ' ' + rightBtn).addClass("over");
            }
        }
        $(el + ' ' + itemsDiv).css('transform', 'translateX(' + -translateXval + 'px)');
    }

    function click(ell, ee) {
        var Parent = "#" + $(ee).parent().attr("id");
        var slide = $(Parent).attr("data-slide");
        ResCarousel(ell, Parent, slide);
    }


    resizeModalsContent();
    function resizeModalsContent() {
        var winWidth = $(window).width();
        var videoWidth = 400;
        if(winWidth < 992) {
            videoWidth = 500;
        }
        var ratio = .6665;
        var videoHeight = videoWidth * ratio;
        $('.modalIframe').css('height', videoHeight);
    }


    // Typed string activation
    if($('#typed-strings').length > 0){
        var typed = new Typed('#typed', {
            stringsElement: '#typed-strings',
            typeSpeed: 100,
            backSpeed: 0,
            backDelay: 1000,
            startDelay: 1000,
            loop: true
        });
    }

    if($('#typed-strings2').length > 0){
        var typed = new Typed('#typed2', {
            stringsElement: '#typed-strings2',
            typeSpeed: 100,
            backSpeed: 0,
            backDelay: 1000,
            startDelay: 1000,
            loop: true
        });
    }

    if($('#typed-strings3').length > 0){
        var typed = new Typed('#typed3', {
            stringsElement: '#typed-strings3',
            typeSpeed: 100,
            backSpeed: 0,
            backDelay: 1000,
            startDelay: 1000,
            loop: true
        });
    }
});