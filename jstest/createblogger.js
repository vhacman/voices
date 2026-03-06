
let apiurl = "http://localhost:8080/voices/api/users";


let blogger = {
    firstName:"Ferdinando",
    lastName:"P.",
    dob:"1980-02-05",
    username:"JavaSifu",
    email:"f.p@email.it",
    password:"pippo",
    role:"BLOGGER"

};


fetch(apiurl, {
    method:"POST",
    headers:{
        "Content-Type":"application/json"
    },
    body:JSON.stringify(blogger)
})
// fetch() restituisce una Promise<Response>, non i dati direttamente.
// La Response è un oggetto HTTP grezzo: contiene status, headers, ecc.,
// ma il body non è ancora stato letto. Chiamando .json() diciamo a fetch 
// di leggere il body e convertirlo da stringa JSON a oggetto JavaScript.
// Senza questa riga, nel .then successivo avremmo la Response e non i dati,
// quindi json.id risulterebbe undefined.
.then(response=>{
    // Controlliamo lo status HTTP prima di leggere il body:
    // se il server ha risposto con un errore (4xx o 5xx), lanciamo
    // un'eccezione con un messaggio chiaro invece di procedere.
    if (!response.ok)
        throw new Error(response.status + " " + response.statusText);
    return response.json();
})
.then(json=>{
    console.log("Saved with id " + json.id);
})
.catch(error=>{
    console.log(error);
});

