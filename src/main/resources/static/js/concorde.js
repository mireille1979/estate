let ctx = setDefaultVal($("meta[name='ctx']").attr("content"), "");
let table = undefined;
let options = {
    background: "rgba(255, 255, 255, 0.9)"
}

function setDefaultVal(value, defaultVal) {
    return (value === undefined) ? defaultVal : value;
}

function deleteItem(id, url){
    if(confirm("Voulez vous vraiment supprimer cet élément ?")){
        fetch(ctx + '/' + url + '?id=' + id, false);
    }
}

function toggleStanding(id, studentId, standingId, url){
    let query = '';
    if(id !== null) query = 'id=' + id;
    if(studentId !== null){
        query += (query.length === 0 ? '' : '&') + 'studentId=' + studentId;
    }
    if(standingId !== null){
        query += (query.length === 0 ? '' : '&') + 'standingId=' + standingId;
    }
    console.log(ctx + '/' + url + '?' + query);
    fetch(ctx + '/' + url + '?' + query, false);
}


function invokeActionOnItems(url, action){
    let n = table.rows({selected: true}).count();
    if(n === 0){
        alert('Aucun élément sélectionné');
    }else{
        let params = [];
        let rows = table.$("tr");
        for(let row of rows){
            if($(row).hasClass('selected')) params.push('ids=' + $(row).attr('data-id'));
        }
        params = params.join("&");
        if(confirm("Voulez-vous vraiment " + action + (n === 1 ? ' cet élément ?' : ' ces ' + n + ' éléments ?'))){
            fetch(ctx + '/' + url + '?' + params, false);
        }
    }
}

function initPagination(){
    let paginator = $('#pagination');
    if(paginator.length !== 0){
        paginator.pagination({
            dataSource: Array.from(Array(parseInt(paginator.attr('title'))).keys()),
            pageSize: 1,
            pageNumber: parseInt(paginator.attr('aria-placeholder')),
            showGoInput: true,
            showGoButton: true,
            triggerPagingOnInit: false,
            callback: function(data, pagination) {
                if(paginator.hasClass('data-search')){
                    $('#current-page').val(pagination.pageNumber);
                    $('#search-button-id').click();
                }else{
                    fetch(ctx + '/' + paginator.attr('aria-label') + '?page=' + pagination.pageNumber);
                }
            }
        })
    }
}
function submitForm(event){
    let isMe = $(event.submitter).hasClass('save-me');
    if(!isMe) event.preventDefault();
    let form = $(event.target);
    let changePassword = $(event.submitter).hasClass('change-password');
    if(changePassword){
        let oldPassword = document.getElementById("old-password").value;
        let newPassword = document.getElementById("new-password").value;
        let confirmPassword = document.getElementById("confirm-password").value;
        if(oldPassword === newPassword){
            alert('Entrez un nouveau mot de passe');
            return false;
        }else if(newPassword !== confirmPassword){
            alert('Le mot de passe de confirmation est incorrect');
            return false;
        }
    }
    let multiple = $(event.submitter).hasClass('save-multiple');
    let actionUrl = form.attr('action') + (multiple ? '?multiple=true' : '');
    $.ajax({
        type: 'POST',
        url: actionUrl,
        data: form.serialize(),
        success: function (data) {
            $("#page-wrapper").html($($.parseHTML(data)).find("#page-wrapper").html());
            $("#user-menu").html($($.parseHTML(data)).find("#user-menu").html());
            initDataList();
            initPagination();
            bindLazyLink("#page-wrapper");
            bindLazyLink("#user-menu");
        }
    })
}
function submitMultipartForm(event){
    event.preventDefault();
    let form = $(event.target);
    let multiple = $(event.submitter).hasClass('save-multiple');
    let actionUrl = form.attr('action') + (multiple ? '?multiple=true' : '');
    let d = new FormData(event.target);
    $.ajax({
        type: 'POST',
        url: actionUrl,
        contentType: false,
        processData: false,
        data: d,
        success: function (data) {
            $('#page-wrapper').html($($.parseHTML(data)).find("#page-wrapper").html());
            initDataList();
            initPagination();
            bindLazyLink("#page-wrapper");
        }
    })
}

jQuery.ajaxSetup({
    beforeSend: function() {
        $('#page-wrapper').LoadingOverlay("show", options);
    },
    complete: function(){
        $('#page-wrapper').LoadingOverlay("hide", options);
        $('#page-wrapper').scrollTop(0);
    },
    statusCode: {
        401: function(){
            fetch(ctx + '/error/401', false);
        },
        403: function(){
            fetch(ctx + '/error/403', false);
        },
        404: function(){
            fetch(ctx + '/error/404', false);
        },
        500: function(){
            fetch(ctx + '/error/500', false);
        }
    }
});

function initPopover(){
    $('#data-list').tooltip({
        selector: "[data-toggle=tooltip]",
        container: "body"
    })
    $("[data-toggle=popover]").popover({
        html: true,
        container: 'body',
        delay: {
            hide: 100,
        },
        content: function () {
            let id = $(this).attr('data-target');
            return $("#" + id).html();
        }
    });
}

function initDataList() {
    $('.searchable-select-box').select2();
    let list = $('#data-list');
    let buttons = [
        {
            text: '<i class="fa fa-file-excel-o"></i> Excel',
            extend: 'excel',
            footer: true,
            title: function () { return list.attr('data-pdf-title'); },
            filename: function () { return list.attr('data-pdf-name'); },
            exportOptions: {
                stripHtml: true,
                columns: ':not(.action)',
            }
        },
        {
            text: '<i class="fa fa-file-pdf-o"></i> PDF',
            extend: 'pdfHtml5',
            footer: true,
            title: function () { return list.attr('data-pdf-title'); },
            filename: function () { return list.attr('data-pdf-name'); },
            orientation: list.hasClass("landscape") ? 'landscape' : 'portrait',
            pageSize: list.hasClass("format-a3") ? 'A3' : 'A4',
            exportOptions: {
                stripHtml: true,
                columns: ':not(.action)',
                modifier: {
                    alignment: 'center',
                },
            },
            customize: function(doc){
                let widths = [];
                $(list).find('thead tr:first-child th:not(.action)').each(function(){
                    widths.push('auto');
                });
                doc.defaultStyle.fontSize = 10;
                // Set the fontsize for the table header
                doc.styles.tableHeader.fontSize = 11;
                doc.styles.tableHeader.alignment = 'left';
                let now = new Date();
                let jsDate = (now.getDate() < 10 ? '0' : '') + now.getDate() + '-' + (now.getMonth() + 1 < 10 ? '0' : '') + (now.getMonth() + 1) + '-'+now.getFullYear();
                doc['footer']=(function(page, pages) {
                    return {
                        columns: [
                            {
                                alignment: 'left',
                                text: ['Créé le : ', { text: jsDate.toString() }]
                            },
                            {
                                alignment: 'right',
                                text: ['Page ', { text: page.toString() },	' / ',	{ text: pages.toString() }]
                            }
                        ],
                        margin: 20
                    }
                });
                if(widths.length > 0){
                    widths[list.hasClass("auto-0") ? 1 : 0] = '*';
                    doc.content[1].table.widths = widths;
                }
                let objLayout = {};
                objLayout['hLineWidth'] = function(i) { return .5; };
                objLayout['vLineWidth'] = function(i) { return .5; };
                objLayout['hLineColor'] = function(i) { return '#aaa'; };
                objLayout['vLineColor'] = function(i) { return '#aaa'; };
                objLayout['paddingLeft'] = function(i) { return 4; };
                objLayout['paddingRight'] = function(i) { return 4; };
                doc.content[1].layout = objLayout;
            }
        },
        /*{
            text: '<i class="fa fa-eye"></i> Visibilité des colonnes',
            extend: 'colvis',
        },*/
    ];
    if(list.hasClass("can-report")) buttons = [
        {
            text: '<i class="fa fa-calculator"></i> Comptabilité',
            action: function () {
                $('#report-items').click();
            }
        },
        ...buttons
    ]
    if(list.hasClass("can-delete")) buttons = [
        {
            text: '<i class="fa fa-trash"></i> Supprimer',
            action: function () {
                $('#delete-items').click();
            }
        },
        ...buttons
    ]
    if(list.hasClass("can-add")) buttons = [
        {
            text: '<i class="fa fa-plus-circle"></i> Ajouter',
            action: function () {
                $('#add-item').click();
            }
        },
        ...buttons
    ]
    if(list.hasClass("column-filtering"))$('#data-list thead tr')
        .clone(true)
        .addClass('filters')
        .appendTo('#data-list thead');
    table = list.DataTable({
        dom: 'Bfrtip',
        buttons: buttons,
        aaSorting: [],
        pageLength: 50,
        responsive: true,
        paging: list.hasClass("paging"),
        searching: list.hasClass("searching"),
        orderCellsTop: true,
        fixedHeader: true,
        columnDefs:  [
            list.hasClass("include-last-sort") ? {} : {orderable: false, targets: -1},
            list.hasClass("exclude-first-sort") ? {orderable: false, targets: 0} : {},
            list.hasClass("multiple-selection") ? {
                'targets': 0,
                'checkboxes': {
                    'selectRow': true,
                    'selectAllPages': false,
                }
            } : {}
        ],
        select: {
            style: 'multi',
            selector: 'td:first-child input:checkbox'
        },
        ...(list.hasClass("fixed-columns") && {
                scrollX: true,
                scrollCollapse: true,
                fixedColumns:  {
                    left: list.attr('data-columns-left'),
                    right: list.attr('data-columns-right')
                }
            }
        ),
        fnDrawCallback: function() {
            initPopover();
            fancyBoxBinding();
        },
    });
}

function fetch(url, flush = true){
    $.get(url, function(data) {
        let wrapper = $($.parseHTML(data)).find("#page-wrapper");
        if(wrapper.length === 0) window.location = ctx + '/237in';
        $('#page-wrapper').html(wrapper.html());
        initDataList();
        initPagination();
        bindLazyLink("#page-wrapper");
        if(flush){
            let title = $($.parseHTML(data)).filter('title').text();
            document.title = title;
            window.history.replaceState({}, title, url);
        }
    });
}

function bindLazyLink(parent = ''){
    let selector = (parent === '' ? '' : parent + ' ') + ".lazy-link";
    $(selector).each(function () {
        let elt = this;
        elt.addEventListener("click", function(e){
            e.preventDefault();
            let url = $(this).attr('href');
            fetch(url);
        })
    });
    initPopover();
    fancyBoxBinding();
}

function fancyBoxBinding(){
    Fancybox.bind('[data-fancybox]', {
       /* Html: {
          videoAutoplay: false,
        },*/
        Toolbar: {
            enabled: true,
            display: {
                left: [],
                middle: ["prev", "infobar", "next"],
                right: ["slideshow", "fullscreen", "download", "thumbs", "close"],
            },
        },
        Thumbs: {
            type: "classic",
            showOnStart: false,
        }
    });
}

$(document).ready(function(){
    bindLazyLink();
    initDataList();
    initPagination();
});