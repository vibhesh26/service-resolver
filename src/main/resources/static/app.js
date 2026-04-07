const apiBase = window.location.pathname.replace(/\/index\.html$/, '').replace(/\/$/, '') + '/api/services';

const notify = (element, message) => {
    element.textContent = message;
};

const fetchGraph = async () => {
    const response = await fetch(`${apiBase}/all`);
    if (!response.ok) {
        throw new Error(`Unable to load graph: ${response.statusText}`);
    }
    return response.json();
};

const postService = async (serviceName, dependencies) => {
    const response = await fetch(`${apiBase}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ serviceName, dependencies })
    });

    return response.json().then(data => {
        if (!response.ok) {
            throw new Error(data || response.statusText);
        }
        return data;
    });
};

const getImpacted = async (serviceName) => {
    const response = await fetch(`${apiBase}/impacted-services?service=${encodeURIComponent(serviceName)}`);
    return response.json().then(data => {
        if (!response.ok) {
            throw new Error(data || response.statusText);
        }
        return data;
    });
};

const getDeployment = async (page, size) => {
    const response = await fetch(`${apiBase}/deployment-order?page=${page}&size=${size}`);
    return response.json().then(data => {
        if (!response.ok) {
            throw new Error(data || response.statusText);
        }
        return data;
    });
};

const drawGraph = (graphData, highlightService, impactedServices, deploymentOrder) => {
    const svg = document.getElementById('dependency-graph');
    const width = svg.clientWidth;
    const height = svg.clientHeight;
    svg.innerHTML = '';

    const services = new Set();
    Object.entries(graphData).forEach(([service, deps]) => {
        services.add(service);
        deps.forEach(dep => services.add(dep));
    });

    const nodes = Array.from(services).sort();
    if (!nodes.length) {
        svg.innerHTML = '<text x="20" y="40" fill="#475569">No services available yet.</text>';
        return;
    }

    const nodePositions = {};
    const radius = Math.min(width, height) / 2 - 120;
    const centerX = width / 2;
    const centerY = height / 2;

    nodes.forEach((name, index) => {
        const angle = (Math.PI * 2 * index) / nodes.length;
        nodePositions[name] = {
            x: centerX + radius * Math.cos(angle),
            y: centerY + radius * Math.sin(angle)
        };
    });

    const defs = document.createElementNS('http://www.w3.org/2000/svg', 'defs');
    defs.innerHTML = `
      <marker id="arrow" markerWidth="8" markerHeight="8" refX="8" refY="4" orient="auto" markerUnits="strokeWidth">
        <path d="M0,0 L8,4 L0,8 Z" fill="#475569" />
      </marker>
    `;
    svg.appendChild(defs);

    Object.entries(graphData).forEach(([service, dependencies]) => {
        dependencies.forEach(dependency => {
            const start = nodePositions[service];
            const end = nodePositions[dependency];
            if (!start || !end) return;

            const line = document.createElementNS('http://www.w3.org/2000/svg', 'path');
            const dx = end.x - start.x;
            const dy = end.y - start.y;
            const midX = start.x + dx * 0.5;
            const midY = start.y + dy * 0.5;
            const curve = `M ${start.x} ${start.y} Q ${midX} ${midY - 40} ${end.x} ${end.y}`;
            line.setAttribute('d', curve);
            line.setAttribute('class', 'edge-line');
            line.setAttribute('marker-end', 'url(#arrow)');
            svg.appendChild(line);
        });
    });

    nodes.forEach(name => {
        const { x, y } = nodePositions[name];
        const group = document.createElementNS('http://www.w3.org/2000/svg', 'g');
        group.setAttribute('transform', `translate(${x}, ${y})`);

        const circle = document.createElementNS('http://www.w3.org/2000/svg', 'circle');
        circle.setAttribute('r', '32');
        circle.setAttribute('class', 'node-circle');
        circle.setAttribute('fill', '#e2e8f0');

        const label = document.createElementNS('http://www.w3.org/2000/svg', 'text');
        label.setAttribute('text-anchor', 'middle');
        label.setAttribute('dominant-baseline', 'middle');
        label.setAttribute('class', 'node-label');
        label.textContent = name;

        if (name === highlightService) {
            circle.setAttribute('fill', '#fbbf24');
            circle.setAttribute('stroke', '#b45309');
            circle.setAttribute('stroke-width', '2');
        } else if (impactedServices && impactedServices.includes(name)) {
            circle.setAttribute('fill', '#86efac');
            circle.setAttribute('stroke', '#15803d');
            circle.setAttribute('stroke-width', '2');
        } else if (deploymentOrder) {
            const position = deploymentOrder.indexOf(name);
            if (position >= 0) {
                const hue = Math.round((position / deploymentOrder.length) * 120);
                circle.setAttribute('fill', `hsl(${hue}, 70%, 75%)`);
            }
        }

        group.appendChild(circle);
        group.appendChild(label);
        svg.appendChild(group);
    });
};

const updateGraph = async (highlightService, impactedServices, deploymentOrder) => {
    const graphData = await fetchGraph();
    drawGraph(graphData, highlightService, impactedServices, deploymentOrder);
    const message = highlightService
        ? `Graph focused on ${highlightService}`
        : 'Loaded graph data for all services.';
    document.getElementById('graph-message').textContent = message;
};

const init = () => {
    const createBtn = document.getElementById('open-create-btn');
    const createModal = document.getElementById('create-modal');
    const cancelCreate = document.getElementById('cancel-create-btn');
    const submitCreate = document.getElementById('submit-create-btn');
    const createResult = document.getElementById('create-result');
    const impactedBtn = document.getElementById('get-impacted-btn');
    const deploymentBtn = document.getElementById('get-deployment-btn');
    const refreshGraph = document.getElementById('refresh-graph-btn');

    createBtn.addEventListener('click', () => {
        createModal.classList.remove('hidden');
        createResult.textContent = '';
    });

    cancelCreate.addEventListener('click', () => {
        createModal.classList.add('hidden');
    });

    submitCreate.addEventListener('click', async () => {
        const serviceName = document.getElementById('create-service-name').value.trim();
        const dependencies = document.getElementById('create-dependencies').value
            .split(',')
            .map(dep => dep.trim())
            .filter(Boolean);

        createResult.textContent = 'Saving...';
        try {
            const result = await postService(serviceName, dependencies);
            createResult.textContent = `Created ${result.serviceName} with ${result.dependencies.length} dependencies.`;
            await updateGraph();
        } catch (error) {
            createResult.textContent = `Error: ${error.message}`;
        }
    });

    impactedBtn.addEventListener('click', async () => {
        const serviceName = document.getElementById('impacted-service-input').value.trim();
        const output = document.getElementById('impacted-output');
        output.textContent = 'Loading...';

        try {
            const data = await getImpacted(serviceName);
            output.textContent = JSON.stringify(data, null, 2);
            await updateGraph(serviceName, data.impactedServices);
        } catch (error) {
            output.textContent = `Error: ${error.message}`;
        }
    });

    deploymentBtn.addEventListener('click', async () => {
        const page = Number(document.getElementById('deployment-page').value);
        const size = Number(document.getElementById('deployment-size').value);
        const output = document.getElementById('deployment-output');
        output.textContent = 'Loading...';

        try {
            const data = await getDeployment(page, size);
            output.textContent = JSON.stringify(data, null, 2);
            const services = data.services || [];
            await updateGraph(null, null, services);
        } catch (error) {
            output.textContent = `Error: ${error.message}`;
        }
    });

    refreshGraph.addEventListener('click', async () => {
        document.getElementById('graph-message').textContent = 'Refreshing graph...';
        await updateGraph();
    });

    updateGraph().catch(error => {
        document.getElementById('graph-message').textContent = `Graph error: ${error.message}`;
    });
};

window.addEventListener('DOMContentLoaded', init);
