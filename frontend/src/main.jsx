import React, { useEffect, useMemo, useState } from "react";
import { createRoot } from "react-dom/client";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis
} from "recharts";
import {
  Activity,
  CreditCard,
  Gauge,
  KeyRound,
  LayoutDashboard,
  LogOut,
  Plus,
  RefreshCw,
  ShieldCheck,
  Trash2,
  Zap
} from "lucide-react";
import { apiClient } from "./api/client";
import "./styles.css";

const navItems = [
  { id: "dashboard", label: "Dashboard", icon: LayoutDashboard },
  { id: "apis", label: "My APIs", icon: Zap },
  { id: "keys", label: "API Keys", icon: KeyRound },
  { id: "usage", label: "Usage", icon: Activity },
  { id: "billing", label: "Billing", icon: CreditCard },
  { id: "settings", label: "Settings", icon: ShieldCheck }
];

const dailyUsage = [
  { day: "Mon", requests: 820, errors: 18 },
  { day: "Tue", requests: 1040, errors: 22 },
  { day: "Wed", requests: 960, errors: 16 },
  { day: "Thu", requests: 1320, errors: 31 },
  { day: "Fri", requests: 1510, errors: 27 },
  { day: "Sat", requests: 1190, errors: 20 },
  { day: "Sun", requests: 1430, errors: 24 }
];

const responseCodes = [
  { code: "2xx", value: 82, color: "#2fbf8f" },
  { code: "4xx", value: 13, color: "#f0a23a" },
  { code: "5xx", value: 5, color: "#ee6c4d" }
];

function App() {
  const [activeView, setActiveView] = useState("dashboard");
  const [session, setSession] = useState(() => JSON.parse(localStorage.getItem("meterflow.session") || "null"));
  const [mode, setMode] = useState("login");
  const [authForm, setAuthForm] = useState({ name: "", email: "", password: "" });
  const [apiForm, setApiForm] = useState({ name: "", baseUrl: "", description: "" });
  const [apis, setApis] = useState([]);
  const [keys, setKeys] = useState([]);
  const [bills, setBills] = useState([]);
  const [message, setMessage] = useState("");

  const client = useMemo(() => apiClient(session?.token), [session]);

  useEffect(() => {
    if (!session) return;
    localStorage.setItem("meterflow.session", JSON.stringify(session));
    refreshData();
  }, [session]);

  async function refreshData() {
    try {
      const [apiList, keyList, billList] = await Promise.all([
        client.get("/api/myapis"),
        client.get("/apikey/mykeys"),
        client.get("/billing").catch(() => [])
      ]);
      setApis(apiList);
      setKeys(keyList);
      setBills(billList);
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function submitAuth(event) {
    event.preventDefault();
    const payload = mode === "register" ? { ...authForm, role: "OWNER" } : authForm;
    try {
      const data = await client.post(`/auth/${mode === "register" ? "register" : "login"}`, payload);
      setSession(data);
      setMessage("Signed in successfully");
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function createApi(event) {
    event.preventDefault();
    try {
      const created = await client.post("/api/create", apiForm);
      setApis((current) => [created, ...current]);
      setApiForm({ name: "", baseUrl: "", description: "" });
      setMessage("API created");
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function generateKey(apiId, planType) {
    try {
      const key = await client.post(`/apikey/generate/${apiId}`, { planType });
      setKeys((current) => [key, ...current]);
      setMessage("API key generated");
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function revokeKey(id) {
    try {
      const updated = await client.post(`/apikey/revoke/${id}`, {});
      setKeys((current) => current.map((key) => (key.id === id ? updated : key)));
      setMessage("API key revoked");
    } catch (error) {
      setMessage(error.message);
    }
  }

  function logout() {
    localStorage.removeItem("meterflow.session");
    setSession(null);
    setApis([]);
    setKeys([]);
  }

  if (!session) {
    return (
      <main className="auth-screen">
        <section className="auth-panel">
          <div>
            <p className="eyebrow">Usage-based API billing</p>
            <h1>MeterFlow</h1>
            <p className="muted">Create APIs, issue keys, enforce limits, proxy traffic, and bill from usage.</p>
          </div>
          <form onSubmit={submitAuth} className="stack">
            {mode === "register" && (
              <input value={authForm.name} onChange={(e) => setAuthForm({ ...authForm, name: e.target.value })} placeholder="Name" />
            )}
            <input value={authForm.email} onChange={(e) => setAuthForm({ ...authForm, email: e.target.value })} placeholder="Email" />
            <input value={authForm.password} onChange={(e) => setAuthForm({ ...authForm, password: e.target.value })} placeholder="Password" type="password" />
            <button className="primary" type="submit">{mode === "login" ? "Login" : "Create account"}</button>
          </form>
          <button className="text-button" onClick={() => setMode(mode === "login" ? "register" : "login")}>
            {mode === "login" ? "Need an account?" : "Already have an account?"}
          </button>
          {message && <p className="notice">{message}</p>}
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand"><Gauge size={24} /> MeterFlow</div>
        <nav>
          {navItems.map((item) => {
            const Icon = item.icon;
            return (
              <button key={item.id} className={activeView === item.id ? "active" : ""} onClick={() => setActiveView(item.id)}>
                <Icon size={18} /> {item.label}
              </button>
            );
          })}
        </nav>
        <button className="logout" onClick={logout}><LogOut size={18} /> Logout</button>
      </aside>

      <main className="workspace">
        <header className="topbar">
          <div>
            <p className="eyebrow">Owner console</p>
            <h2>{navItems.find((item) => item.id === activeView)?.label}</h2>
          </div>
          <button className="icon-button" onClick={refreshData} title="Refresh"><RefreshCw size={18} /></button>
        </header>
        {message && <p className="notice">{message}</p>}
        {activeView === "dashboard" && <Dashboard keys={keys} apis={apis} />}
        {activeView === "apis" && <Apis apis={apis} apiForm={apiForm} setApiForm={setApiForm} createApi={createApi} generateKey={generateKey} />}
        {activeView === "keys" && <Keys keys={keys} revokeKey={revokeKey} />}
        {activeView === "usage" && <Usage />}
        {activeView === "billing" && <Billing bills={bills} client={client} session={session} />}
        {activeView === "settings" && <Settings session={session} />}
      </main>
    </div>
  );
}

function Dashboard({ keys, apis }) {
  return (
    <>
      <section className="metric-grid">
        <Metric title="Total Requests" value="8,270" tone="mint" />
        <Metric title="Active Keys" value={keys.filter((key) => key.status !== "REVOKED").length || 0} tone="steel" />
        <Metric title="Revenue" value="₹41.50" tone="coral" />
        <Metric title="APIs" value={apis.length} tone="ink" />
      </section>
      <section className="content-grid">
        <Panel title="Daily Requests"><UsageArea /></Panel>
        <Panel title="Response Codes"><CodeBars /></Panel>
      </section>
    </>
  );
}

function Apis({ apis, apiForm, setApiForm, createApi, generateKey }) {
  return (
    <section className="content-grid">
      <Panel title="Create API">
        <form onSubmit={createApi} className="stack">
          <input value={apiForm.name} onChange={(e) => setApiForm({ ...apiForm, name: e.target.value })} placeholder="weather" />
          <input value={apiForm.baseUrl} onChange={(e) => setApiForm({ ...apiForm, baseUrl: e.target.value })} placeholder="https://api.example.com" />
          <textarea value={apiForm.description} onChange={(e) => setApiForm({ ...apiForm, description: e.target.value })} placeholder="Description" />
          <button className="primary" type="submit"><Plus size={16} /> Create API</button>
        </form>
      </Panel>
      <Panel title="Registered APIs">
        <div className="table-list">
          {apis.map((api) => (
            <div className="row" key={api.id}>
              <div><strong>{api.name}</strong><span>{api.baseUrl}</span></div>
              <button className="secondary" onClick={() => generateKey(api.id, "FREE")}><KeyRound size={16} /> Key</button>
            </div>
          ))}
          {!apis.length && <p className="muted">No APIs yet.</p>}
        </div>
      </Panel>
    </section>
  );
}

function Keys({ keys, revokeKey }) {
  return (
    <Panel title="API Keys">
      <div className="table-list">
        {keys.map((key) => (
          <div className="row" key={key.id}>
            <div><strong>{key.planType}</strong><span>{key.keyValue}</span></div>
            <button className="danger" onClick={() => revokeKey(key.id)}><Trash2 size={16} /> Revoke</button>
          </div>
        ))}
        {!keys.length && <p className="muted">Generate keys from the My APIs page.</p>}
      </div>
    </Panel>
  );
}

function Usage() {
  return <section className="content-grid"><Panel title="Monthly Usage"><UsageArea /></Panel><Panel title="Status Mix"><CodeBars /></Panel></section>;
}

function Billing({ bills, client, session }) {
  const [subscribing, setSubscribing] = useState(false);

  const handleSubscribe = async (planType) => {
    try {
      setSubscribing(true);
      const razorpayKey = import.meta.env.VITE_RAZORPAY_KEY_ID;
      if (!razorpayKey) {
        throw new Error("Razorpay public key is not configured");
      }
      if (!window.Razorpay) {
        throw new Error("Razorpay checkout failed to load");
      }

      const data = await client.post("/subscriptions/create", { planType });
      
      const options = {
        key: razorpayKey,
        subscription_id: data.subscriptionId,
        name: "MeterFlow PRO",
        description: "Unlimited API requests",
        handler: async function (response) {
          try {
            const verifyData = await client.post("/subscriptions/verify", {
              paymentId: response.razorpay_payment_id,
              subscriptionId: response.razorpay_subscription_id,
              signature: response.razorpay_signature
            });
            if (verifyData.success) {
              const updatedSession = { ...session, planType: "PRO" };
              setSession(updatedSession);
              localStorage.setItem("meterflow.session", JSON.stringify(updatedSession));
              alert("Subscription successful. You are now on the PRO plan.");
            } else {
              alert("Verification Failed");
            }
          } catch(err) {
            alert(err.message);
          }
        },
        prefill: {
          name: session?.name || "",
          email: session?.email || ""
        },
        theme: {
          color: "#2fbf8f"
        }
      };
      
      const rzp = new window.Razorpay(options);
      rzp.on('payment.failed', function (response){
        alert(response.error.description);
      });
      rzp.open();
    } catch (err) {
      alert(err.message);
    } finally {
      setSubscribing(false);
    }
  };

  return (
    <>
      <Panel title="PRO Plan Subscription">
        <p className="muted" style={{marginBottom: "1rem"}}>Upgrade to PRO for unlimited requests and advanced analytics.</p>
        <button className="primary" onClick={() => handleSubscribe("PRO")} disabled={subscribing}>
          {subscribing ? "Processing..." : "Subscribe to PRO - ₹999/mo"}
        </button>
      </Panel>
      <br/>
      <Panel title="Invoices">
        <div className="table-list">
          {bills.map((bill) => (
            <div className="row" key={bill.id}>
              <div><strong>{bill.month}</strong><span>{bill.totalRequests} requests</span></div>
              <span className="pill">₹{bill.amount} {bill.paymentStatus}</span>
            </div>
          ))}
          {!bills.length && <p className="muted">Invoices appear after the monthly billing job runs.</p>}
        </div>
      </Panel>
    </>
  );
}

function Settings({ session }) {
  return <Panel title="Account"><p className="muted">{session.name} · {session.email} · {session.role} · {session.planType || "FREE"}</p></Panel>;
}

function Metric({ title, value, tone }) {
  return <article className={`metric ${tone}`}><span>{title}</span><strong>{value}</strong></article>;
}

function Panel({ title, children }) {
  return <section className="panel"><h3>{title}</h3>{children}</section>;
}

function UsageArea() {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <AreaChart data={dailyUsage}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e7eaf0" />
        <XAxis dataKey="day" />
        <YAxis />
        <Tooltip />
        <Area dataKey="requests" stroke="#2fbf8f" fill="#bcebdc" />
      </AreaChart>
    </ResponsiveContainer>
  );
}

function CodeBars() {
  return (
    <ResponsiveContainer width="100%" height={260}>
      <BarChart data={responseCodes}>
        <CartesianGrid strokeDasharray="3 3" stroke="#e7eaf0" />
        <XAxis dataKey="code" />
        <YAxis />
        <Tooltip />
        <Bar dataKey="value">
          {responseCodes.map((entry) => <Cell key={entry.code} fill={entry.color} />)}
        </Bar>
      </BarChart>
    </ResponsiveContainer>
  );
}

createRoot(document.getElementById("root")).render(<App />);
