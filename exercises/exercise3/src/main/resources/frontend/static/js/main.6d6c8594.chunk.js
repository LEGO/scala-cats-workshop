(this["webpackJsonpclient-app"]=this["webpackJsonpclient-app"]||[]).push([[0],[,,,,,function(e,t,a){e.exports=a(19)},,,,,,,,function(e,t,a){},function(e,t,a){},function(e,t,a){},function(e,t,a){},function(e,t,a){},function(e,t,a){},function(e,t,a){"use strict";a.r(t);var n=a(0),r=a.n(n),s=a(3),c=a.n(s),l=a(1),u=a(4),m={requestedUsername:"",client:null},o=r.a.createContext(m),i=function(e){var t=e.username,a=e.children;return m.client=new u.w3cwebsocket("ws://127.0.0.1:4000/chat?username=".concat(t)),m.requestedUsername=t,r.a.createElement(o.Provider,{value:m},a)},d=(a(13),function(e){var t=e.createUser,a=Object(n.useState)(""),s=Object(l.a)(a,2),c=s[0],u=s[1];return r.a.createElement("div",{className:"modal-overlay"},r.a.createElement("div",{className:"modal-wrapper"},r.a.createElement("h2",null,"Insert your username"),r.a.createElement("form",{onSubmit:function(e){e.preventDefault(),t(c),u("")}},r.a.createElement("input",{type:"text",placeholder:"your username",value:c,minLength:1,required:!0,autoFocus:!0,onChange:function(e){e.preventDefault(),u(e.target.value)}}))))}),f=(a(14),function(e){var t=e.username,a=e.users;return r.a.createElement("div",{className:"users"},r.a.createElement("div",null,"@",t),r.a.createElement("h3",null,"users"),r.a.createElement("div",null,a.map((function(e,t){return r.a.createElement("p",{key:t},e)}))))}),p=(a(15),function(e){return new Date(1e3*e).toLocaleTimeString()}),E=function(e){var t=e.data;if(console.log(t),t.isEmote){var a=t.text+" <span class='message-handler-date'>"+p(t.timestamp)+"</span>";return r.a.createElement("div",{className:"message"},r.a.createElement("div",{className:"message-handler"},r.a.createElement("pre",{className:"message-text",dangerouslySetInnerHTML:{__html:a}})))}return r.a.createElement("div",{className:"message"},r.a.createElement("div",{className:"message-handler"},r.a.createElement("span",{className:"message-handler-user"},t.username),r.a.createElement("span",{className:"message-handler-date"},p(t.timestamp))),r.a.createElement("pre",{className:"message-text",dangerouslySetInnerHTML:{__html:t.text}}))},v=function(){var e=Object(n.useContext)(o).client,t=Object(n.useState)(""),a=Object(l.a)(t,2),s=a[0],c=a[1];return r.a.createElement("form",{onSubmit:function(t){t.preventDefault(),e.send(JSON.stringify({text:s})),c("")}},r.a.createElement("input",{type:"text",placeholder:"write to chat..",value:s,minLength:1,required:!0,autoFocus:!0,onChange:function(e){e.preventDefault(),c(e.target.value)}}))},g=(a(16),function(e){var t=e.messages,a=Object(n.useRef)();return Object(n.useEffect)((function(){a.current.scrollIntoView({behavior:"smooth"})}),[t]),r.a.createElement("div",{className:"chat"},r.a.createElement("div",{className:"chat-messages"},t.map((function(e,t){return r.a.createElement(E,{key:t,data:e})})),r.a.createElement("div",{ref:a})),r.a.createElement("div",{className:"chat-input"},r.a.createElement(v,null)))}),h=(a(17),function(){var e=Object(n.useContext)(o),t=e.client,a=e.requestedUsername,s=Object(n.useState)(a),c=Object(l.a)(s,2),u=c[0],m=c[1],i=Object(n.useState)([]),d=Object(l.a)(i,2),p=d[0],E=d[1],v=Object(n.useState)([]),h=Object(l.a)(v,2),b=h[0],O=h[1];return Object(n.useEffect)((function(){t.onmessage=function(e){var t=JSON.parse(e.data);console.log("chat app",t),t.Connected&&(m(t.Connected.username),E(t.Connected.userList)),t.UserJoined&&E(t.UserJoined.userList),t.UserLeft&&E(t.UserLeft.userList),t.Message&&O(b.concat(t.Message))}}),[t,b,u]),r.a.createElement("div",{className:"chat-app"},r.a.createElement(f,{username:u,users:p}),r.a.createElement(g,{messages:b}))}),b=function(){var e=Object(n.useState)(""),t=Object(l.a)(e,2),a=t[0],s=t[1];return r.a.createElement(r.a.Fragment,null,""===a?r.a.createElement(d,{createUser:s}):r.a.createElement(i,{username:a},r.a.createElement(h,null)))};a(18);c.a.render(r.a.createElement(b,null),document.getElementById("root"))}],[[5,1,2]]]);
//# sourceMappingURL=main.6d6c8594.chunk.js.map