document.addEventListener('DOMContentLoaded', function() {
    // 登录表单处理（不影响注册跳转，暂时忽略）
    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            // 原有登录逻辑...
        });
    }

    // 注册表单处理（重点！确保事件绑定成功）
    const registerForm = document.getElementById('registerForm');
    if (registerForm) {
        // 绑定提交事件
        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault(); // 阻止默认提交

            // 获取表单数据
            const username = document.getElementById('regUsername').value;
            const password = document.getElementById('regPassword').value;
            const email = document.getElementById('regEmail').value;

            try {
                // 发送注册请求
                const response = await fetch('http://localhost:8080/api/auth/register', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ username, password, email })
                });

                // 解析响应（兼容JSON和文本）
                const contentType = response.headers.get('content-type');
                const data = contentType && contentType.includes('application/json')
                    ? await response.json()
                    : { message: await response.text() };

                // 注册成功：执行跳转
                if (response.ok) {
                    alert('注册成功！即将跳转到登录页');
                    // 强制跳转登录页（核心逻辑）
                    window.location.href = 'http://localhost:8080/login.html';
                } else {
                    // 注册失败：显示错误
                    document.getElementById('registerError').textContent = data.message || '注册失败';
                }
            } catch (error) {
                document.getElementById('registerError').textContent = '网络错误，请重试';
                console.error('注册请求失败:', error);
            }
        });
    } else {
        // 调试提示：若打印此信息，说明未找到registerForm元素
        console.error('未找到id为"registerForm"的表单，请检查register.html');
    }
});