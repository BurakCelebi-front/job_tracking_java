import { useEffect, useState } from "react";
import type { FormEvent } from "react";
import { getCompanies, createCompany, deleteCompany } from "../api/companies";
import type { CompanyRequest } from "../api/companies";
import type { Company } from "../types";
import { useAuth } from "../context/AuthContext";
import { useNavigate } from "react-router-dom";

export default function Companies() {
  const [companies, setCompanies] = useState<Company[]>([]);
  const [name, setName] = useState("");
  const [website, setWebsite] = useState("");
  const [location, setLocation] = useState("");
  const [error, setError] = useState("");
  const { logout } = useAuth();
  const navigate = useNavigate();

  const loadCompanies = async () => {
    try {
      const data = await getCompanies();
      setCompanies(data);
    } catch (err) {
      setError("The companies could not be loaded.");
    }
  };

  useEffect(() => {
    loadCompanies();
  }, []);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError("");
    const data: CompanyRequest = { name, website, location };
    try {
      await createCompany(data);
      setName("");
      setWebsite("");
      setLocation("");
      loadCompanies();
    } catch (err) {
      setError("The company could not be added.");
    }
  };

  const handleDelete = async (id: number) => {
    try {
      await deleteCompany(id);
      loadCompanies();
    } catch (err) {
      setError("The company could not be deleted (there may be associated applications).");
    }
  };

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div style={{ maxWidth: 600, margin: "40px auto" }}>
      <div style={{ display: "flex", justifyContent: "space-between" }}>
        <h2>Şirketler</h2>
        <button onClick={handleLogout}>Logout</button>
      </div>

      <form onSubmit={handleSubmit} style={{ marginBottom: 20 }}>
        <input
          placeholder="Company name"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          placeholder="Website"
          value={website}
          onChange={(e) => setWebsite(e.target.value)}
        />
        <input
          placeholder="Location"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
        />
        <button type="submit">Add</button>
      </form>

      {error && <p style={{ color: "red" }}>{error}</p>}

      <ul>
        {companies.map((c) => (
          <li key={c.id}>
            {c.name} — {c.location} ({c.website})
            <button onClick={() => handleDelete(c.id)} style={{ marginLeft: 10 }}>
              Delete
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
}