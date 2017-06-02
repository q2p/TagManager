var page;

function search() {
	location = '/ideas/search/1/'+encodeURI(document.getElementsByTagName('input')[0].value);
}

function init() {
	page = window.location.search;
	page = parseInt(page.substring(page.indexOf('?page=')+6));
	
	var xhr = new XMLHttpRequest();
	
	xhr.open('GET', '/ideasData?page='+page, true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		
		console.log(this.responseText);
		
		var data = JSON.parse(this.responseText);
				
		var offset = setList('posts', data[1], data[0][0]);
		setList('notes', data[2], offset);
		
		var t = '';
		
		const pagesOffset = 2;
		var beg = Math.max(1, page-pagesOffset);
		var end = Math.min(data[0][1], page+pagesOffset);
		
		if(beg > 1) t += '<a href="ideas?page=1">1</a>';
		if(beg > 2) t += '<p>...</p>';
		for(var i = beg; i <= end; i++) {
			if(i==page) {
				t += '<a id="cur_page" onclick="goto()">'+i+'</a>';
			} else t += '<a href="ideas?page='+i+'">'+i+'</a>';
		}
		if(end < data[0][1]-1) t += '<p>...</p>';
		if(end < data[0][1]) t += '<a href="ideas?page='+data[0][1]+'">'+data[0][1]+'</a>';
		
		document.getElementById('wrapper').innerHTML = t;
	};
	
	xhr.send();
}
//
function setList(id, array, offset) {
	var t = '';
	for(var i = 0; i < array.length; i++){
		t += '<a class="idea_block" href="idea?id='+offset+'">'+ offset +'. '+array[i]+'</a>';
		offset++;
	}
	
	document.getElementById(id).getElementsByClassName('selector')[0].innerHTML = t;
	if(t == '') document.getElementById(id).style.display = 'none';
	
	return offset;
}
//
function goto() {
	var answer = prompt('На кокую страницу переместиться?', page);
	if(answer == null) return;
	answer = answer.trim();
	var goto_page = parseInt(answer);
	if(goto_page == NaN || answer != (''+goto_page)) {
		alert('Вы ввели не число');
		setTimeout(goto, 0);
	}
	window.location = 'ideas?page='+goto_page;
}
//
function createNote() {
	if(!confirm('Создать заметку?')) return;
	var xhr = new XMLHttpRequest();

	xhr.open('POST', '/ideaCreateNote', true);
	
	xhr.onreadystatechange = function() {
		if (this.readyState != 4) return;
		if(this.responseText == 'false') {
			alert('Не удалось создать заметку.');
			return;
		}
		var td = document.getElementsByClassName('post_title').length+1;
		document.getElementById('notes_selector').innerHTML += '<div class="post_block"><div class="post_title">' + ('0000'+td).substr(-4) + '. Новая заметка</div><div class="post_inside"></div></div>';
		editPost(td);
	};
	
	xhr.send();
}