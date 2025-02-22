document.addEventListener("DOMContentLoaded", function() {
    const subscribedTagsElement = document.getElementById("subscribedTags");

    let subscribedTags = [];
    
    function displayTagData(tagPath, value) {
        let tagValue = document.getElementById(`tag-${tagPath}-value`)
        tagValue.innerHTML = `<pre><code> ${value} </code></pre>`
    }

    function getUserInfo() {
        return {
            type: 'basic',
            username: document.getElementById('username').value,
            password: document.getElementById('password').value
        }
    }

    function subscribeTag() {
        const tagPathElement = document.getElementById("tagPath");
        const tagPath = tagPathElement.value
        if (!tagPath) return;
        
        if (!subscribedTags.includes(tagPath)) {
            subscribedTags.push(tagPath);
            subscribedTagsElement.appendChild(createTagDisplayElement(tagPath));
            createSession()
        }

        tagPathElement.value = ""
    }

    function unsubscribeTag(tagPath, tagElement) {
        const index = subscribedTags.indexOf(tagPath);
        if (index > -1) {
            subscribedTags.splice(index, 1);
            subscribedTagsElement.removeChild(tagElement);
            createSession()
        }
    }

    function createTagDisplayElement(tagPath) {
        let tagElement = document.createElement("div");
        tagElement.id = `tag-${tagPath}`
        tagElement.className = "tag";
        tagElement.innerHTML = `<span>${tagPath}</span>`;

        let unsubscribeButton = document.createElement("button");
        unsubscribeButton.innerText = "X";
        unsubscribeButton.onclick = function() {
            unsubscribeTag(tagPath, tagElement);
        };

        let tagValue = document.createElement("label");
        tagValue.id = `tag-${tagPath}-value`;

        tagElement.appendChild(unsubscribeButton);
        tagElement.appendChild(tagValue);
        return tagElement;
    }



    let eventSource = null;
    function createSession() {
        // Close the existing session, if it exists.
        if (eventSource) {
            eventSource.close();
        }
    
        // Create the body of the session request.
        const requestBody = JSON.stringify({
            streams: {
                tag: {
                    paths: subscribedTags,
                    events: ['tag_change']
                },
                license: { },
            },
            auth: getUserInfo()
        })
        console.log(`post-body: ${requestBody}`)

        // Create a new session.
        fetch("/embr/event-stream/session", {
            method: "POST",
            mode: "cors",
            body: requestBody
        }).then( response => {
            // Get the response as JSON.
            return response.json()
        }).then( sessionInfo => {
            console.log(`post-response: ${sessionInfo}`)

            // Connect to the event source.
            const url = `/embr/event-stream/session/${sessionInfo.data.session_id}`
            eventSource = new EventSource(url);
            const tagStream = sessionInfo.data.streams.tag
            const tags = tagStream.tags

            // When a tag change occurs, display the tags new value.
            eventSource.addEventListener('tag_change', (e) => {
                const tagChangeData = JSON.parse(e.data)
                const tagPath = tags[tagChangeData.tag_id].path
                const tagDataString = JSON.stringify(tagChangeData)
                displayTagData(tagPath, tagDataString)
            })

            // When the session is opened...
            eventSource.onopen = function (e) {
                console.log('EventSource opened.');
            }
        
            // If an error occurs...
            eventSource.onerror = function (e) {
                console.log('EventSource errored.');
            }
        })
    }

    window.subscribeTag = subscribeTag;
    window.unsubscribeTag = unsubscribeTag;
});
